package com.ecomove.auth.service.impl;

import com.ecomove.auth.client.UserGrpcClient;
import com.ecomove.auth.constant.AuthMessageConstant;
import com.ecomove.auth.constant.AuthRedisConstant;
import com.ecomove.auth.dto.request.LoginRequest;
import com.ecomove.auth.dto.request.VerifyOtpRequest;
import com.ecomove.auth.dto.response.LoginResponse;
import com.ecomove.auth.dto.response.TokenResponse;
import com.ecomove.auth.entity.Credential;
import com.ecomove.auth.enums.Status;
import com.ecomove.auth.event.SmsOtpEvent;
import com.ecomove.auth.repository.CredentialRepository;
import com.ecomove.auth.service.AuthService;
import com.ecomove.auth.util.AuthUtils;
import com.ecomove.auth.util.JwtUtils;
import com.ecomove.common.exception.HttpException;
import com.ecomove.common.kafka.KafkaProducerHelper;
import com.ecomove.grpc.user.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtils jwtUtils;
    private final UserGrpcClient userGrpcClient;
    private final KafkaProducerHelper kafkaProducerHelper;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Credential credential = credentialRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED);
        }

        if (credential.getStatus() != Status.ACTIVE) {
            throw new HttpException(403, AuthMessageConstant.ERROR_ACCOUNT_LOCKED);
        }

        String otpCode = AuthUtils.generateOtp();
        String sessionId = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(AuthRedisConstant.OTP_PREFIX + sessionId, otpCode, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(AuthRedisConstant.ATTEMPTS_PREFIX + sessionId, "0", 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(AuthRedisConstant.SESSION_USER_PREFIX + sessionId, credential.getPhone(), 5, TimeUnit.MINUTES);

        // Publish to Kafka using the shared KafkaProducerHelper
        SmsOtpEvent event = SmsOtpEvent.builder()
                .phone(credential.getPhone())
                .otp(otpCode)
                .build();
        
        kafkaProducerHelper.sendEvent("sms_otp_events", credential.getPhone(), event);

        return LoginResponse.builder()
                .sessionId(sessionId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse verifyOtp(VerifyOtpRequest request) {
        String sessionId = request.getSessionId();
        String attemptsStr = redisTemplate.opsForValue().get(AuthRedisConstant.ATTEMPTS_PREFIX + sessionId);
        
        if (attemptsStr != null && Integer.parseInt(attemptsStr) >= 5) {
            throw new HttpException(429, AuthMessageConstant.ERROR_TOO_MANY_ATTEMPTS);
        }

        String cachedOtp = redisTemplate.opsForValue().get(AuthRedisConstant.OTP_PREFIX + sessionId);
        if (cachedOtp == null) {
            throw new HttpException(400, AuthMessageConstant.ERROR_OTP_EXPIRED);
        }

        if (!request.getOtp().equals(cachedOtp)) {
            redisTemplate.opsForValue().increment(AuthRedisConstant.ATTEMPTS_PREFIX + sessionId);
            throw new HttpException(401, AuthMessageConstant.ERROR_INVALID_OTP);
        }

        // Clean up OTP session from Redis
        redisTemplate.delete(AuthRedisConstant.OTP_PREFIX + sessionId);
        redisTemplate.delete(AuthRedisConstant.ATTEMPTS_PREFIX + sessionId);

        String phone = redisTemplate.opsForValue().get(AuthRedisConstant.SESSION_USER_PREFIX + sessionId);
        if (phone == null) {
            throw new HttpException(400, AuthMessageConstant.ERROR_OTP_EXPIRED);
        }
        redisTemplate.delete(AuthRedisConstant.SESSION_USER_PREFIX + sessionId);

        Credential credential = credentialRepository.findByPhone(phone)
                .orElseThrow(() -> new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED));

        // Fetch user profile via gRPC (handles user not found or deleted scenarios)
        UserProfileResponse userProfile = userGrpcClient.getUserProfile(credential.getUserId());

        // Generate Tokens
        String accessToken = jwtUtils.generateAccessToken(credential.getUserId(), credential.getRole(), userProfile);
        String refreshToken = jwtUtils.generateRefreshToken(credential.getUserId());

        // Save refresh token hash in Redis (valid for 7 days)
        String rtHash = AuthUtils.hashToken(refreshToken);
        redisTemplate.opsForValue().set(
                AuthRedisConstant.REFRESH_TOKEN_PREFIX + credential.getUserId(),
                rtHash,
                7,
                TimeUnit.DAYS
        );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}

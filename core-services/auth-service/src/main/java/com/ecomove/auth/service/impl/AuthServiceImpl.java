package com.ecomove.auth.service.impl;

import com.ecomove.auth.client.UserGrpcClient;
import com.ecomove.auth.config.CookieProperties;
import com.ecomove.auth.constant.AuthMessageConstant;
import com.ecomove.auth.constant.AuthRedisConstant;
import com.ecomove.auth.dto.request.LoginRequest;
import com.ecomove.auth.dto.request.RefreshTokenRequest;
import com.ecomove.auth.dto.request.VerifyOtpRequest;
import com.ecomove.auth.dto.response.LoginResponse;
import com.ecomove.auth.dto.response.TokenResponse;
import com.ecomove.auth.dto.response.UserSessionResponse;
import com.ecomove.auth.entity.Credential;
import com.ecomove.auth.enums.ClientType;
import com.ecomove.auth.enums.Role;
import com.ecomove.auth.enums.Status;
import com.ecomove.auth.event.SmsOtpEvent;
import com.ecomove.auth.repository.CredentialRepository;
import com.ecomove.auth.service.AuthService;
import com.ecomove.auth.util.AuthUtils;
import com.ecomove.auth.util.CookieUtils;
import com.ecomove.auth.util.JwtUtils;
import com.ecomove.common.exception.HttpException;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.Set;
import com.ecomove.common.kafka.KafkaProducerHelper;
import com.ecomove.grpc.user.UserProfileResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
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
    private final CookieProperties cookieProperties;

    private static final Set<Role> INTERNAL_ROLES = Set.of(Role.ADMIN, Role.STAFF);

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Credential credential = credentialRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED));

        if (INTERNAL_ROLES.contains(credential.getRole())) {
            throw new HttpException(403, AuthMessageConstant.ERROR_SSO_REQUIRED);
        }

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
    public TokenResponse verifyOtp(VerifyOtpRequest request, String clientTypeHeader, HttpServletResponse httpResponse) {
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

        ClientType clientType;
        try {
            clientType = ClientType.valueOf(clientTypeHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            clientType = ClientType.MOBILE;
        }

        if (clientType == ClientType.WEB) {
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.buildAccessTokenCookie(accessToken, cookieProperties).toString());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.buildRefreshTokenCookie(refreshToken, cookieProperties).toString());
            return null;
        }

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public UserSessionResponse getProfileFromCookie(String accessToken) {
        if (accessToken == null || accessToken.isEmpty() || !jwtUtils.validateToken(accessToken)) {
            throw new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED);
        }

        // Check blacklist
        String hashToken = AuthUtils.hashToken(accessToken);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(AuthRedisConstant.BLACKLIST_TOKEN_PREFIX + hashToken))) {
            throw new HttpException(401, AuthMessageConstant.ERROR_TOKEN_BLACKLISTED);
        }

        try {
            Claims claims = jwtUtils.getClaimsFromToken(accessToken);
            String userIdStr = claims.getSubject();
            String email = claims.get("email", String.class);
            String fullName = claims.get("fullName", String.class);
            String avatar = claims.get("avatar", String.class);
            String role = claims.get("role", String.class);

            if (userIdStr == null) {
                throw new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED);
            }

            return UserSessionResponse.builder()
                    .userId(userIdStr)
                    .fullName(fullName != null ? fullName : "")
                    .email(email != null ? email : "")
                    .avatar(avatar != null ? avatar : "")
                    .role(role != null ? role : "")
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse access token cookie", e);
            throw new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED);
        }
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request, String refreshTokenCookie, String clientTypeHeader, HttpServletResponse httpResponse) {
        ClientType clientType;
        try {
            clientType = ClientType.valueOf(clientTypeHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            clientType = ClientType.MOBILE;
        }

        String refreshToken = null;
        if (clientType == ClientType.WEB) {
            refreshToken = refreshTokenCookie;
        } else {
            if (request != null && request.getRefreshToken() != null) {
                refreshToken = request.getRefreshToken();
            }
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new HttpException(400, AuthMessageConstant.ERROR_INVALID_REFRESH_TOKEN);
        }

        if (!jwtUtils.validateToken(refreshToken)) {
            throw new HttpException(401, AuthMessageConstant.ERROR_INVALID_REFRESH_TOKEN);
        }

        Claims claims;
        try {
            claims = jwtUtils.getClaimsFromToken(refreshToken);
        } catch (Exception e) {
            throw new HttpException(401, AuthMessageConstant.ERROR_INVALID_REFRESH_TOKEN);
        }

        String userIdStr = claims.getSubject();
        if (userIdStr == null) {
            throw new HttpException(401, AuthMessageConstant.ERROR_INVALID_REFRESH_TOKEN);
        }
        UUID userId = UUID.fromString(userIdStr);

        // Verify token hash in Redis
        String cachedHash = redisTemplate.opsForValue().get(AuthRedisConstant.REFRESH_TOKEN_PREFIX + userIdStr);
        if (cachedHash == null || !cachedHash.equals(AuthUtils.hashToken(refreshToken))) {
            throw new HttpException(401, AuthMessageConstant.ERROR_INVALID_REFRESH_TOKEN);
        }

        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED));

        if (credential.getStatus() != Status.ACTIVE) {
            throw new HttpException(403, AuthMessageConstant.ERROR_ACCOUNT_LOCKED);
        }

        UserProfileResponse userProfile = userGrpcClient.getUserProfile(userId);

        // Generate new access token
        String newAccessToken = jwtUtils.generateAccessToken(userId, credential.getRole(), userProfile);

        if (clientType == ClientType.WEB) {
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.buildAccessTokenCookie(newAccessToken, cookieProperties).toString());
            return null;
        }

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String accessToken, String clientTypeHeader, HttpServletResponse httpResponse) {
        if (accessToken != null && !accessToken.isEmpty() && jwtUtils.validateToken(accessToken)) {
            try {
                Claims claims = jwtUtils.getClaimsFromToken(accessToken);
                String userIdStr = claims.getSubject();
                
                if (userIdStr != null) {
                    // 1. Invalidate Refresh Token
                    redisTemplate.delete(AuthRedisConstant.REFRESH_TOKEN_PREFIX + userIdStr);
                }

                // 2. Blacklist Access Token
                Date expiration = claims.getExpiration();
                if (expiration != null) {
                    long remainingTimeMillis = expiration.getTime() - System.currentTimeMillis();
                    if (remainingTimeMillis > 0) {
                        String hashToken = AuthUtils.hashToken(accessToken);
                        redisTemplate.opsForValue().set(
                                AuthRedisConstant.BLACKLIST_TOKEN_PREFIX + hashToken,
                                "LOGGED_OUT",
                                remainingTimeMillis,
                                TimeUnit.MILLISECONDS
                        );
                    }
                }
            } catch (Exception e) {
                log.error("Error processing token during logout", e);
            }
        }

        // Handle Web Client cookies
        ClientType clientType;
        try {
            clientType = ClientType.valueOf(clientTypeHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            clientType = ClientType.MOBILE;
        }

        if (clientType == ClientType.WEB) {
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.clearCookie("access_token", "/").toString());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.clearCookie("refresh_token", "/api/v1/auth/refresh").toString());
        }
    }
}

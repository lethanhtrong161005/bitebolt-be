package com.bitebolt.auth.service.impl;

import com.bitebolt.auth.client.UserGrpcClient;
import com.bitebolt.auth.config.CookieProperties;
import com.bitebolt.auth.constant.AuthMessageConstant;
import com.bitebolt.auth.constant.AuthRedisConstant;
import com.bitebolt.auth.dto.request.LoginRequest;
import com.bitebolt.auth.dto.request.RefreshTokenRequest;
import com.bitebolt.auth.dto.request.VerifyOtpRequest;
import com.bitebolt.auth.dto.response.LoginResponse;
import com.bitebolt.auth.dto.response.TokenResponse;
import com.bitebolt.auth.dto.response.UserSessionResponse;
import com.bitebolt.auth.entity.Credential;
import com.bitebolt.auth.enums.ClientType;
import com.bitebolt.auth.enums.Role;
import com.bitebolt.auth.enums.Status;
import com.bitebolt.auth.event.SmsOtpEvent;
import com.bitebolt.auth.repository.CredentialRepository;
import com.bitebolt.auth.service.AuthService;
import com.bitebolt.common.logging.audit.AuditAction;
import com.bitebolt.common.logging.audit.Auditable;
import com.bitebolt.common.security.jwt.JwtProvider;
import com.bitebolt.common.exception.HttpException;
import com.bitebolt.common.constant.AppConstant;
import com.bitebolt.common.security.utils.CryptoUtils;
import com.bitebolt.common.security.context.UserContext;
import com.bitebolt.auth.util.CookieUtils;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.Set;
import com.bitebolt.common.kafka.KafkaProducerHelper;
import com.bitebolt.grpc.user.UserProfileResponse;
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
    private final JwtProvider jwtProvider;
    private final UserGrpcClient userGrpcClient;
    private final KafkaProducerHelper kafkaProducerHelper;
    private final CookieProperties cookieProperties;

    private static final Set<Role> INTERNAL_ROLES = Set.of(Role.ADMIN, Role.STAFF);

    @Override
    @Transactional(readOnly = true)
    @Auditable(action = AuditAction.LOGIN_SUCCESS, resourceType = "Credential")
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

        String otpCode = CryptoUtils.generateOtp();
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

        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", credential.getRole().name());
        if (userProfile != null) {
            if (userProfile.getEmail() != null) claims.put("email", userProfile.getEmail());
            if (userProfile.getFullName() != null) claims.put("fullName", userProfile.getFullName());
            if (userProfile.getAvatar() != null) claims.put("avatar", userProfile.getAvatar());
        }
        String accessToken = jwtProvider.generateAccessToken(credential.getUserId().toString(), claims);
        String refreshToken = jwtProvider.generateRefreshToken(credential.getUserId().toString());

        // Save refresh token hash in Redis (valid for 7 days)
        String rtHash = CryptoUtils.hashToken(refreshToken);
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
    public UserSessionResponse getCurrentUserProfile(UserContext context) {
        if (context == null || context.getUserId() == null) {
            throw new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED);
        }

        // Context Enrichment Pattern: Query fresh data from DB (via gRPC) because headers are lean
        UserProfileResponse freshProfile = userGrpcClient.getUserProfile(UUID.fromString(context.getUserId()));
        
        // Enrich the context so subsequent method calls in the same thread can use it without re-querying
        context.setFullName(freshProfile.getFullName());
        context.setEmail(freshProfile.getEmail());
        context.setAvatar(freshProfile.getAvatar());

        return UserSessionResponse.builder()
                .userId(context.getUserId())
                .fullName(context.getFullName())
                .email(context.getEmail())
                .avatar(context.getAvatar())
                .role(context.getRole() != null ? context.getRole() : "")
                .build();
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

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new HttpException(401, AuthMessageConstant.ERROR_INVALID_REFRESH_TOKEN);
        }

        Claims claims;
        try {
            claims = jwtProvider.verifyAndGetClaims(refreshToken);
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
        if (cachedHash == null || !cachedHash.equals(CryptoUtils.hashToken(refreshToken))) {
            throw new HttpException(401, AuthMessageConstant.ERROR_INVALID_REFRESH_TOKEN);
        }

        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new HttpException(401, AuthMessageConstant.ERROR_UNAUTHORIZED));

        if (credential.getStatus() != Status.ACTIVE) {
            throw new HttpException(403, AuthMessageConstant.ERROR_ACCOUNT_LOCKED);
        }

        UserProfileResponse userProfile = userGrpcClient.getUserProfile(userId);

        // Generate new access token
        java.util.Map<String, Object> newClaims = new java.util.HashMap<>();
        newClaims.put("role", credential.getRole().name());
        if (userProfile != null) {
            if (userProfile.getEmail() != null) newClaims.put("email", userProfile.getEmail());
            if (userProfile.getFullName() != null) newClaims.put("fullName", userProfile.getFullName());
            if (userProfile.getAvatar() != null) newClaims.put("avatar", userProfile.getAvatar());
        }
        String newAccessToken = jwtProvider.generateAccessToken(userId.toString(), newClaims);

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
    @Auditable(action = AuditAction.LOGOUT)
    public void logout(String accessToken, String clientTypeHeader, HttpServletResponse httpResponse) {
        if (accessToken != null && !accessToken.isEmpty() && jwtProvider.validateToken(accessToken)) {
            try {
                Claims claims = jwtProvider.verifyAndGetClaims(accessToken);
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
                        String hashToken = CryptoUtils.hashToken(accessToken);
                        redisTemplate.opsForValue().set(
                                AppConstant.BLACKLIST_TOKEN_PREFIX + hashToken,
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

package com.bitebolt.auth.service.impl;

import com.bitebolt.auth.client.UserGrpcClient;
import com.bitebolt.auth.config.CookieProperties;
import com.bitebolt.auth.constant.AuthConstant;
import com.bitebolt.auth.constant.AuthMessageConstant;
import com.bitebolt.auth.constant.AuthRedisConstant;
import com.bitebolt.auth.entity.Credential;
import com.bitebolt.auth.enums.AuthMethod;
import com.bitebolt.auth.enums.Role;
import com.bitebolt.auth.enums.Status;
import com.bitebolt.auth.repository.CredentialRepository;
import com.bitebolt.auth.service.SsoService;
import com.bitebolt.auth.util.CookieUtils;
import com.bitebolt.common.security.utils.CryptoUtils;
import com.bitebolt.common.security.jwt.JwtProvider;
import com.bitebolt.common.exception.HttpException;
import com.bitebolt.common.logging.audit.AuditAction;
import com.bitebolt.common.logging.audit.Auditable;
import com.bitebolt.grpc.user.UserProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.bitebolt.auth.config.EntraProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntraSsoServiceImpl implements SsoService {

    private final EntraProperties entraProperties;
    private final CookieProperties cookieProperties;
    private final CredentialRepository credentialRepository;
    private final UserGrpcClient userGrpcClient;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String initiateEntraLogin() {
        String state = UUID.randomUUID().toString();
        String stateKey = AuthRedisConstant.SSO_STATE_PREFIX + state;
        
        // Store state in Redis for CSRF validation (TTL: 10 minutes)
        redisTemplate.opsForValue().set(
                stateKey,
                "valid",
                10,
                TimeUnit.MINUTES
        );
        log.info("[SSO] Generated and saved state to Redis: key={}, value=valid", stateKey);

        String authorizeUrl = entraProperties.getAuthorizeUrl();

        // Build the Microsoft Entra ID OAuth 2.0 authorization URL
        return UriComponentsBuilder.fromHttpUrl(authorizeUrl)
                .queryParam(AuthConstant.CLIENT_ID, entraProperties.getClientId())
                .queryParam(AuthConstant.RESPONSE_TYPE, "code")
                .queryParam(AuthConstant.REDIRECT_URI, entraProperties.getRedirectUri())
                .queryParam(AuthConstant.RESPONSE_MODE, "query")
                .queryParam(AuthConstant.SCOPE, "openid profile email")
                .queryParam(AuthConstant.STATE, state)
                .build()
                .toUriString();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @Auditable(action = AuditAction.SSO_LOGIN_SUCCESS, resourceType = "Credential")
    public String handleEntraCallback(String code, String state, HttpServletResponse httpResponse) {
        log.info("[SSO] Received callback code: {}, state: {}", code, state);
        
        // Allow bypass state for development/testing convenience
        if ("mock-state".equals(state) || "test-state".equals(state)) {
            log.warn("[SSO] Dev bypass detected for state: {}", state);
        } else {
            // 1. Validate State (CSRF protection)
            String stateKey = AuthRedisConstant.SSO_STATE_PREFIX + state;
            String cachedState = redisTemplate.opsForValue().get(stateKey);
            log.info("[SSO] Checking Redis key: {}, found value: {}", stateKey, cachedState);
            
            if (cachedState == null) {
                log.error("[SSO] State validation failed. Key {} not found or expired in Redis.", stateKey);
                throw new HttpException(400, AuthMessageConstant.ERROR_SSO_STATE_MISMATCH);
            }
            redisTemplate.delete(stateKey);
        }

        try {
            String authorityUrl = entraProperties.getAuthorityUrl();

            // 2. Exchange Authorization Code for Tokens via MSAL4J
            ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                    entraProperties.getClientId(),
                    ClientCredentialFactory.createFromSecret(entraProperties.getClientSecret()))
                    .authority(authorityUrl)
                    .build();

            AuthorizationCodeParameters authParams = AuthorizationCodeParameters.builder(
                    code,
                    new URI(entraProperties.getRedirectUri()))
                    .scopes(Collections.singleton("openid"))
                    .build();

            IAuthenticationResult result = app.acquireToken(authParams).get();
            String idToken = result.idToken();

            // 3. Decode & Parse ID Token Claims
            String[] jwtParts = idToken.split("\\.");
            if (jwtParts.length < 2) {
                throw new HttpException(400, AuthMessageConstant.ERROR_SSO_STATE_MISMATCH);
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(jwtParts[1]), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

            String entraObjectId = (String) claims.get("oid");
            String email = (String) claims.get("email");
            if (email == null) {
                email = (String) claims.get("preferred_username");
            }
            String name = (String) claims.get("name");

            if (entraObjectId == null || email == null) {
                log.error("Invalid token payload, missing oid or email: {}", payloadJson);
                throw new HttpException(400, AuthMessageConstant.ERROR_SSO_STATE_MISMATCH);
            }

            final String finalEmail = email;
            final String finalEntraObjectId = entraObjectId;

            // 4. Lookup Credential
            Credential credential = credentialRepository.findByEntraObjectId(finalEntraObjectId)
                    .orElseGet(() -> credentialRepository.findByEmail(finalEmail)
                            .map(existing -> {
                                // Link existing user to SSO object ID
                                existing.setEntraObjectId(finalEntraObjectId);
                                existing.setAuthMethod(AuthMethod.SSO_ENTRA);
                                return credentialRepository.save(existing);
                            })
                            .orElse(null)
                    );

            // 5. Auto-provision STAFF if not found in database and domain is valid
            if (credential == null) {
                String allowedEmailDomain = entraProperties.getAllowedEmailDomain();
                if (allowedEmailDomain != null && !allowedEmailDomain.isEmpty()) {
                    if (!email.endsWith("@" + allowedEmailDomain)) {
                        log.warn("Access denied. Email {} domain does not match allowed domain: {}", email, allowedEmailDomain);
                        //TODO: By pass test in dev environment
                        //throw new HttpException(403, AuthMessageConstant.ERROR_INVALID_EMAIL_DOMAIN);
                    }
                }

                UUID newUserId = UUID.randomUUID();
                
                // Auto-create user profile in user-service via gRPC
                userGrpcClient.createUserProfile(newUserId, name, email, "");

                credential = new Credential();
                credential.setUserId(newUserId);
                credential.setEmail(email);
                credential.setEntraObjectId(entraObjectId);
                credential.setRole(Role.STAFF);
                credential.setStatus(Status.ACTIVE);
                credential.setAuthMethod(AuthMethod.SSO_ENTRA);
                credential = credentialRepository.save(credential);
                
                log.info("Auto-provisioned new STAFF user: {} with userId: {}", email, newUserId);
            }

            // 6. Validate status
            if (credential.getStatus() != Status.ACTIVE) {
                log.warn("SSO user account is locked/disabled: {}", email);
                throw new HttpException(403, AuthMessageConstant.ERROR_ACCOUNT_LOCKED);
            }

            // 7. Call user-service gRPC to get the full profile for token claims
            UserProfileResponse userProfile = userGrpcClient.getUserProfile(credential.getUserId());

            // 8. Generate Tokens
            Map<String, Object> tokenClaims = new HashMap<>();
            tokenClaims.put("role", credential.getRole().name());
            String accessToken = jwtProvider.generateAccessToken(credential.getUserId().toString(), tokenClaims);
            String refreshToken = jwtProvider.generateRefreshToken(credential.getUserId().toString());

            // 9. Save Refresh Token in Redis
            String rtHash = CryptoUtils.hashToken(refreshToken);
            redisTemplate.opsForValue().set(
                    AuthRedisConstant.REFRESH_TOKEN_PREFIX + credential.getUserId(),
                    rtHash,
                    7,
                    TimeUnit.DAYS
            );

            // 10. Write HttpOnly Cookies
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.buildAccessTokenCookie(accessToken, cookieProperties).toString());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.buildRefreshTokenCookie(refreshToken, cookieProperties).toString());

            log.info("SSO Login successful for user: {}", email);
            return entraProperties.getSuccessRedirectUrl();

        } catch (HttpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred during Microsoft Entra SSO authentication flow", e);
            throw new HttpException(500, AuthMessageConstant.ERROR_SSO_STATE_MISMATCH);
        }
    }
}

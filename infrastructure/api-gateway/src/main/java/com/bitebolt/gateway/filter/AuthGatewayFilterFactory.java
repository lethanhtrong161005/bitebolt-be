package com.bitebolt.gateway.filter;

import com.bitebolt.common.constant.AppConstant;
import com.bitebolt.common.constant.MessageConstant;
import com.bitebolt.gateway.constant.GatewayConstant;
import com.bitebolt.gateway.security.GatewayErrorHelper;
import com.bitebolt.common.security.jwt.JwtProvider;
import com.bitebolt.common.security.utils.CryptoUtils;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * The main Security Filter for the API Gateway.
 *
 * <p>This filter intercepts all incoming requests before they reach the internal microservices. It
 * is responsible for:
 *
 * <ul>
 *   <li>Bypassing public endpoints (Swagger, Auth endpoints).
 *   <li>Extracting the JWT from either the Authorization header or HttpOnly Cookie.
 *   <li>Checking the Redis Blacklist to ensure the token has not been revoked (e.g. via Logout).
 *   <li>Verifying the JWT signature and expiration.
 *   <li>Injecting authenticated user metadata (X-User-Id, X-User-Role) into the request headers for
 *       downstream services.
 * </ul>
 *
 * @author bitebolt Team
 */
@Component
public class AuthGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

  private final JwtProvider jwtProvider;
  private final ReactiveRedisTemplate<String, String> redisTemplate;
  private final GatewayErrorHelper errorHelper;

  public AuthGatewayFilterFactory(
      JwtProvider jwtProvider,
      ReactiveRedisTemplate<String, String> redisTemplate,
      GatewayErrorHelper errorHelper) {
    super(Config.class);
    this.jwtProvider = jwtProvider;
    this.redisTemplate = redisTemplate;
    this.errorHelper = errorHelper;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String path = exchange.getRequest().getURI().getPath();

      // 1. BYPASS LOGIC: Allow Swagger docs, Actuator endpoints, and Public Auth endpoints to pass
      // through
      boolean isPublicEndpoint =
          Arrays.stream(GatewayConstant.PUBLIC_ENDPOINTS).anyMatch(path::contains);

      if (isPublicEndpoint) {
        return chain.filter(exchange);
      }

      // 2. EXTRACT TOKEN
      String token = extractToken(exchange.getRequest());
      if (!StringUtils.hasText(token)) {
        return errorHelper.writeUnauthorized(
            exchange.getResponse(), MessageConstant.ERROR_MISSING_TOKEN);
      }

      // 3. CHECK BLACKLIST
      String hashToken = CryptoUtils.hashToken(token);
      String blacklistKey = AppConstant.BLACKLIST_TOKEN_PREFIX + hashToken;

      return redisTemplate
          .hasKey(blacklistKey)
          .flatMap(
              isBlacklisted -> {
                if (Boolean.TRUE.equals(isBlacklisted)) {
                  return errorHelper.writeUnauthorized(
                      exchange.getResponse(), MessageConstant.ERROR_TOKEN_BLACKLISTED);
                }

                // 4. VERIFY JWT
                Claims claims;
                try {
                  claims = jwtProvider.verifyAndGetClaims(token);
                } catch (Exception e) {
                  return errorHelper.writeUnauthorized(
                      exchange.getResponse(), MessageConstant.ERROR_INVALID_TOKEN);
                }

                // 5. INJECT HEADERS
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                String fullName = claims.get("fullName", String.class);
                String email = claims.get("email", String.class);
                String avatar = claims.get("avatar", String.class);

                ServerHttpRequest.Builder requestBuilder =
                    exchange
                        .getRequest()
                        .mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role);

                if (StringUtils.hasText(fullName)) {
                  requestBuilder.header(
                      "X-User-FullName",
                      java.net.URLEncoder.encode(
                          fullName, java.nio.charset.StandardCharsets.UTF_8));
                }
                if (StringUtils.hasText(email)) {
                  requestBuilder.header("X-User-Email", email);
                }
                if (StringUtils.hasText(avatar)) {
                  requestBuilder.header(
                      "X-User-Avatar",
                      java.net.URLEncoder.encode(avatar, java.nio.charset.StandardCharsets.UTF_8));
                }

                ServerHttpRequest mutatedRequest = requestBuilder.build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
              });
    };
  }

  /**
   * Attempts to extract the JWT token from the incoming HTTP request.
   *
   * <p>Supports two extraction methods to accommodate different client types: 1. Authorization:
   * Bearer <token> (Used by Mobile Apps) 2. access_token Cookie (Used by Web browsers for enhanced
   * security)
   *
   * @param request the current ServerHttpRequest
   * @return the raw JWT string, or null if not found
   */
  private String extractToken(ServerHttpRequest request) {
    // 1. Try Bearer token first (Mobile)
    String bearerToken = request.getHeaders().getFirst("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    // 2. Fallback to HttpOnly Cookie (Web) - WebFlux parsing
    HttpCookie cookie = request.getCookies().getFirst("access_token");
    if (cookie != null) {
      return cookie.getValue();
    }

    // 3. Manual Fallback: Sometimes curl or clients send raw Cookie strings that WebFlux misses
    String cookieHeader = request.getHeaders().getFirst("Cookie");
    if (cookieHeader != null) {
      String[] cookies = cookieHeader.split(";");
      for (String c : cookies) {
        c = c.trim();
        if (c.startsWith("access_token=")) {
          return c.substring("access_token=".length());
        }
      }
    }

    return null;
  }

  public static class Config {
    // configuration properties if needed
  }
}

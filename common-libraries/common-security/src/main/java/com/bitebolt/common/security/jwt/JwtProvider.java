package com.bitebolt.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Enterprise Unified JWT Provider. Extracted from Auth Service and API Gateway to enforce DRY and
 * SOLID principles.
 *
 * <p>Handles the generation, validation, and parsing of JWT tokens. This class uses
 * java.nio.charset.StandardCharsets.UTF_8 for strict key byte extraction.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "jwt.secret")
public class JwtProvider {

  private final SecretKey key;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration:3600}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration:604800}") long refreshTokenExpiration) {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpirationMs = accessTokenExpiration * 1000L;
    this.refreshTokenExpirationMs = refreshTokenExpiration * 1000L;
  }

  /**
   * Generates a new Access Token.
   *
   * @param subject the principal identifier (usually UUID)
   * @param claims any additional claims to embed in the payload (role, fullName, etc.)
   * @return signed JWT string
   */
  public String generateAccessToken(String subject, Map<String, Object> claims) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

    io.jsonwebtoken.JwtBuilder builder =
        Jwts.builder().subject(subject).issuedAt(now).expiration(expiryDate);

    if (claims != null && !claims.isEmpty()) {
      claims.forEach(builder::claim);
    }

    return builder.signWith(key).compact();
  }

  /**
   * Generates a long-lived Refresh Token.
   *
   * @param subject the principal identifier
   * @return signed JWT string
   */
  public String generateRefreshToken(String subject) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

    return Jwts.builder()
        .subject(subject)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(key)
        .compact();
  }

  /**
   * Parses the JWT token, verifying its signature and expiration.
   *
   * @param token the raw JWT string
   * @return the extracted Claims (payload)
   * @throws ExpiredJwtException if token has naturally expired
   * @throws RuntimeException if token is malformed or invalid signature
   */
  public Claims verifyAndGetClaims(String token) {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("JWT is expired: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.warn("Invalid JWT: {}", e.getMessage());
      throw new RuntimeException("Invalid JWT token", e);
    }
  }

  /**
   * Safely validates a token, returning boolean instead of throwing an exception. Useful for
   * preliminary checks where exceptions are too heavy.
   *
   * @param token the raw JWT string
   * @return true if valid and not expired, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}

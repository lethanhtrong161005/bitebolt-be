package com.ecomove.auth.util;

import com.ecomove.auth.config.JwtProperties;
import com.ecomove.auth.enums.Role;
import com.ecomove.grpc.user.UserProfileResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, Role role, UserProfileResponse profile) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration() * 1000L);

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate);

        if (profile != null) {
            if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
                builder.claim("email", profile.getEmail());
            }
            if (profile.getFullName() != null && !profile.getFullName().isEmpty()) {
                builder.claim("fullName", profile.getFullName());
            }
            if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                builder.claim("avatar", profile.getAvatar());
            }
        }

        return builder.signWith(key).compact();
    }

    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration() * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

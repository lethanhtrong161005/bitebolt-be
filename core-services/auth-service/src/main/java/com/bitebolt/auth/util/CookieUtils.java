package com.bitebolt.auth.util;

import com.bitebolt.auth.config.CookieProperties;
import org.springframework.http.ResponseCookie;
import java.time.Duration;

public final class CookieUtils {

  private CookieUtils() {}

  public static ResponseCookie buildAccessTokenCookie(String token, CookieProperties properties) {
    return ResponseCookie.from("access_token", token)
        .httpOnly(true)
        .secure(properties.isSecure())
        .path("/")
        .maxAge(Duration.ofSeconds(properties.getAccessTokenMaxAge()))
        .sameSite(properties.getSameSite())
        .build();
  }

  public static ResponseCookie buildRefreshTokenCookie(String token, CookieProperties properties) {
    return ResponseCookie.from("refresh_token", token)
        .httpOnly(true)
        .secure(properties.isSecure())
        .path("/api/v1/auth/refresh")
        .maxAge(Duration.ofSeconds(properties.getRefreshTokenMaxAge()))
        .sameSite("Strict")
        .build();
  }

  public static ResponseCookie clearCookie(String name, String path) {
    return ResponseCookie.from(name, "").httpOnly(true).path(path).maxAge(0).build();
  }
}

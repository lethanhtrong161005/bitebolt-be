package com.bitebolt.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cookie")
@Data
public class CookieProperties {
  private String domain;
  private boolean secure;
  private String sameSite;
  private int accessTokenMaxAge;
  private int refreshTokenMaxAge;
}

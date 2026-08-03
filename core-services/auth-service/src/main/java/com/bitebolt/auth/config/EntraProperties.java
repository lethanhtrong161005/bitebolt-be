package com.bitebolt.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "entra")
public class EntraProperties {
  private String tenantId;
  private String authorityUrl;
  private String authorizeUrl;
  private String clientId;
  private String clientSecret;
  private String redirectUri;
  private String allowedEmailDomain;
  private String successRedirectUrl;
}

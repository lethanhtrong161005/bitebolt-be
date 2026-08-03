package com.bitebolt.gateway.constant;

/** Global constants for the API Gateway. */
public class GatewayConstant {

  /** List of public endpoints that should bypass JWT authentication. */
  public static final String[] PUBLIC_ENDPOINTS = {
    "/v3/api-docs",
    "/swagger-ui",
    "/actuator",
    "/api/v1/auth/login",
    "/api/v1/auth/verify-otp",
    "/api/v1/auth/refresh",
    "/api/v1/auth/sso"
  };

  private GatewayConstant() {
    // Prevent instantiation
  }
}

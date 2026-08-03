package com.bitebolt.auth.constant;

public final class AuthConstant {
  private AuthConstant() {}

  /** Header dùng để phân biệt Web Admin và Mobile App client */
  public static final String CLIENT_TYPE_HEADER = "X-Client-Type";

  // OIDC / OAuth2 Query Parameter Constants
  public static final String CLIENT_ID = "client_id";
  public static final String RESPONSE_TYPE = "response_type";
  public static final String REDIRECT_URI = "redirect_uri";
  public static final String RESPONSE_MODE = "response_mode";
  public static final String SCOPE = "scope";
  public static final String STATE = "state";
  public static final String CODE = "code";
}

package com.bitebolt.auth.service;

import com.bitebolt.auth.dto.request.LoginRequest;
import com.bitebolt.auth.dto.request.RefreshTokenRequest;
import com.bitebolt.auth.dto.request.VerifyOtpRequest;
import com.bitebolt.auth.dto.response.LoginResponse;
import com.bitebolt.auth.dto.response.TokenResponse;
import com.bitebolt.auth.dto.response.UserSessionResponse;
import jakarta.servlet.http.HttpServletResponse;
import com.bitebolt.common.security.context.UserContext;

public interface AuthService {

  LoginResponse login(LoginRequest request);

  TokenResponse verifyOtp(
      VerifyOtpRequest request, String clientTypeHeader, HttpServletResponse httpResponse);

  UserSessionResponse getCurrentUserProfile(UserContext context);

  TokenResponse refreshToken(
      RefreshTokenRequest request,
      String refreshTokenCookie,
      String clientTypeHeader,
      HttpServletResponse httpResponse);

  void logout(String accessToken, String clientTypeHeader, HttpServletResponse httpResponse);
}

package com.ecomove.auth.service;

import com.ecomove.auth.dto.request.LoginRequest;
import com.ecomove.auth.dto.request.RefreshTokenRequest;
import com.ecomove.auth.dto.request.VerifyOtpRequest;
import com.ecomove.auth.dto.response.LoginResponse;
import com.ecomove.auth.dto.response.TokenResponse;
import com.ecomove.auth.dto.response.UserSessionResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    TokenResponse verifyOtp(VerifyOtpRequest request, String clientTypeHeader, HttpServletResponse httpResponse);

    UserSessionResponse getProfileFromCookie(String accessToken);

    TokenResponse refreshToken(RefreshTokenRequest request, String refreshTokenCookie, String clientTypeHeader, HttpServletResponse httpResponse);

    void logout(String accessToken, String clientTypeHeader, HttpServletResponse httpResponse);
}

package com.ecomove.auth.service;

import com.ecomove.auth.dto.request.LoginRequest;
import com.ecomove.auth.dto.request.VerifyOtpRequest;
import com.ecomove.auth.dto.response.LoginResponse;
import com.ecomove.auth.dto.response.TokenResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    TokenResponse verifyOtp(VerifyOtpRequest request);
}

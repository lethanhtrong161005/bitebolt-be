package com.ecomove.auth.controller;

import com.ecomove.auth.constant.AuthMessageConstant;
import com.ecomove.auth.dto.request.LoginRequest;
import com.ecomove.auth.dto.request.VerifyOtpRequest;
import com.ecomove.auth.dto.response.LoginResponse;
import com.ecomove.auth.dto.response.TokenResponse;
import com.ecomove.auth.service.AuthService;
import com.ecomove.common.dto.ApiResponse;
import com.ecomove.common.utils.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Service", description = "Endpoints for 2FA login verification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Step 1: Verify phone and password, generate and send 6-digit OTP")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ResponseHelper.successWithData(response, AuthMessageConstant.SUCCESS_LOGIN_INITIATED));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Step 2: Verify 6-digit OTP and generate JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        TokenResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ResponseHelper.successWithData(response, AuthMessageConstant.SUCCESS_OTP_VERIFIED));
    }
}

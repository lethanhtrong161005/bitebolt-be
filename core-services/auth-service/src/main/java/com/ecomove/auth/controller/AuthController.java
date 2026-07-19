package com.ecomove.auth.controller;

import com.ecomove.auth.constant.AuthConstant;
import com.ecomove.auth.constant.AuthMessageConstant;
import com.ecomove.auth.dto.request.LoginRequest;
import com.ecomove.auth.dto.request.RefreshTokenRequest;
import com.ecomove.auth.dto.request.VerifyOtpRequest;
import com.ecomove.auth.dto.response.LoginResponse;
import com.ecomove.auth.dto.response.TokenResponse;
import com.ecomove.auth.dto.response.UserSessionResponse;
import com.ecomove.auth.service.AuthService;
import com.ecomove.common.dto.ApiResponse;
import com.ecomove.common.utils.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<?>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader(value = AuthConstant.CLIENT_TYPE_HEADER, defaultValue = "mobile") String clientTypeHeader,
            HttpServletResponse httpResponse) {
        TokenResponse response = authService.verifyOtp(request, clientTypeHeader, httpResponse);

        if (response == null) {
            return ResponseEntity.ok(ResponseHelper.success(AuthMessageConstant.SUCCESS_OTP_VERIFIED));
        }

        return ResponseEntity.ok(ResponseHelper.successWithData(response, AuthMessageConstant.SUCCESS_OTP_VERIFIED));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile from access_token cookie")
    public ResponseEntity<ApiResponse<UserSessionResponse>> getCurrentUser(
            @CookieValue(value = "access_token", required = false) String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ResponseHelper.error(401, AuthMessageConstant.ERROR_UNAUTHORIZED));
        }
        UserSessionResponse profile = authService.getProfileFromCookie(accessToken);
        return ResponseEntity.ok(ResponseHelper.successWithData(profile, "SUCCESS"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<?>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(value = "refresh_token", required = false) String refreshTokenCookie,
            @RequestHeader(value = AuthConstant.CLIENT_TYPE_HEADER, defaultValue = "mobile") String clientTypeHeader,
            HttpServletResponse httpResponse) {
        
        TokenResponse response = authService.refreshToken(request, refreshTokenCookie, clientTypeHeader, httpResponse);

        if (response == null) {
            return ResponseEntity.ok(ResponseHelper.success(AuthMessageConstant.SUCCESS_TOKEN_REFRESHED));
        }

        return ResponseEntity.ok(ResponseHelper.successWithData(response, AuthMessageConstant.SUCCESS_TOKEN_REFRESHED));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and blacklist tokens")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(value = "access_token", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestHeader(value = AuthConstant.CLIENT_TYPE_HEADER, defaultValue = "mobile") String clientTypeHeader,
            HttpServletResponse httpResponse) {
        
        String accessToken = accessTokenCookie;
        if (accessToken == null || accessToken.isEmpty()) {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                accessToken = authorizationHeader.substring(7);
            }
        }

        authService.logout(accessToken, clientTypeHeader, httpResponse);
        
        return ResponseEntity.ok(ResponseHelper.success(AuthMessageConstant.SUCCESS_LOGOUT));
    }
}


package com.bitebolt.auth.controller;

import com.bitebolt.auth.service.SsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/sso")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SSO Authentication", description = "Endpoints for Microsoft Entra ID SSO login")
public class SsoController {

  private final SsoService ssoService;

  @GetMapping({"/entra", "/entra/login"})
  @Operation(summary = "Redirect to Microsoft Entra ID login portal")
  public void initiateEntraLogin(HttpServletResponse response) throws IOException {
    String redirectUrl = ssoService.initiateEntraLogin();
    log.info("Redirecting user to Microsoft login: {}", redirectUrl);
    response.sendRedirect(redirectUrl);
  }

  @GetMapping("/entra/callback")
  @Operation(summary = "Callback endpoint for Microsoft Entra ID authentication code exchange")
  public void handleEntraCallback(
      @RequestParam("code") String code,
      @RequestParam("state") String state,
      HttpServletResponse response)
      throws IOException {
    log.info("Received callback from Microsoft Entra ID with state: {}", state);
    String successRedirectUrl = ssoService.handleEntraCallback(code, state, response);
    log.info("SSO authentication completed. Redirecting to landing page: {}", successRedirectUrl);
    response.sendRedirect(successRedirectUrl);
  }
}

package com.bitebolt.common.logging.filter;

import com.bitebolt.common.logging.constant.AuditConstant;
import com.bitebolt.common.logging.masking.MaskingUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Filter for automatically logging the entire lifecycle of an HTTP request and response.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Bypass:</strong> Ignores Actuator and Swagger endpoints to prevent log pollution.
 *   <li><strong>Wrap Request/Response:</strong> Wraps the request and response objects in caching
 *       wrappers to allow reading the body multiple times (if needed for payload logging).
 *   <li><strong>Pre-handle:</strong> Logs the incoming HTTP method and URI, putting metadata into
 *       MDC.
 *   <li><strong>Execution:</strong> Proceeds with the filter chain.
 *   <li><strong>Post-handle:</strong> Calculates execution duration and logs the outgoing HTTP
 *       status. Cleans up its own MDC keys.
 * </ol>
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Do not log actuator or swagger endpoints
    String path = request.getRequestURI();
    if (path.startsWith("/actuator")
        || path.startsWith("/swagger")
        || path.startsWith("/v3/api-docs")) {
      filterChain.doFilter(request, response);
      return;
    }

    long startTime = System.currentTimeMillis();
    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

    // Request Log
    MDC.put(AuditConstant.MDC_KEY_LOG_TYPE, AuditConstant.LOG_TYPE_REQUEST);
    MDC.put(AuditConstant.MDC_KEY_METHOD, request.getMethod());
    MDC.put(AuditConstant.MDC_KEY_PATH, path);
    log.info("Incoming Request: {} {}", request.getMethod(), path);

    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      int status = responseWrapper.getStatus();

      // Response Log
      MDC.put(AuditConstant.MDC_KEY_LOG_TYPE, AuditConstant.LOG_TYPE_RESPONSE);
      MDC.put(AuditConstant.MDC_KEY_STATUS, String.valueOf(status));
      MDC.put(AuditConstant.MDC_KEY_DURATION_MS, String.valueOf(duration));

      if (status >= 500) {
        log.error(
            "Outgoing Response: {} {} - Status: {} - {}ms",
            request.getMethod(),
            path,
            status,
            duration);
      } else if (status >= 400) {
        log.warn(
            "Outgoing Response: {} {} - Status: {} - {}ms",
            request.getMethod(),
            path,
            status,
            duration);
      } else {
        log.info(
            "Outgoing Response: {} {} - Status: {} - {}ms",
            request.getMethod(),
            path,
            status,
            duration);
      }

      // Clean up MDC keys specific to this filter to prevent leakage to other log statements
      MDC.remove(AuditConstant.MDC_KEY_LOG_TYPE);
      MDC.remove(AuditConstant.MDC_KEY_METHOD);
      MDC.remove(AuditConstant.MDC_KEY_PATH);
      MDC.remove(AuditConstant.MDC_KEY_STATUS);
      MDC.remove(AuditConstant.MDC_KEY_DURATION_MS);

      responseWrapper.copyBodyToResponse();
    }
  }
}

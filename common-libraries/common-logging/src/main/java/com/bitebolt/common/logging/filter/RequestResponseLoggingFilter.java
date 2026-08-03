package com.bitebolt.common.logging.filter;

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
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Bypass:</strong> Ignores Actuator and Swagger endpoints to prevent log pollution.</li>
 *   <li><strong>Wrap Request/Response:</strong> Wraps the request and response objects in caching wrappers to allow reading the body multiple times (if needed for payload logging).</li>
 *   <li><strong>Pre-handle:</strong> Logs the incoming HTTP method and URI, putting metadata into MDC.</li>
 *   <li><strong>Execution:</strong> Proceeds with the filter chain.</li>
 *   <li><strong>Post-handle:</strong> Calculates execution duration and logs the outgoing HTTP status. Cleans up its own MDC keys.</li>
 * </ol>
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Do not log actuator or swagger endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Request Log
        MDC.put("log_type", "REQUEST");
        MDC.put("method", request.getMethod());
        MDC.put("path", path);
        log.info("Incoming Request: {} {}", request.getMethod(), path);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = responseWrapper.getStatus();

            // Response Log
            MDC.put("log_type", "RESPONSE");
            MDC.put("status", String.valueOf(status));
            MDC.put("duration_ms", String.valueOf(duration));

            if (status >= 500) {
                log.error("Outgoing Response: {} {} - Status: {} - {}ms", request.getMethod(), path, status, duration);
            } else if (status >= 400) {
                log.warn("Outgoing Response: {} {} - Status: {} - {}ms", request.getMethod(), path, status, duration);
            } else {
                log.info("Outgoing Response: {} {} - Status: {} - {}ms", request.getMethod(), path, status, duration);
            }

            // Clean up MDC keys specific to this filter to prevent leakage to other log statements
            MDC.remove("log_type");
            MDC.remove("method");
            MDC.remove("path");
            MDC.remove("status");
            MDC.remove("duration_ms");

            responseWrapper.copyBodyToResponse();
        }
    }
}

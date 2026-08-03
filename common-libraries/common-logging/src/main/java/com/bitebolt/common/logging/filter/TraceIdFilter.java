package com.bitebolt.common.logging.filter;

import com.bitebolt.common.constant.AppConstant;
import com.bitebolt.common.logging.constant.AuditConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that intercepts incoming HTTP requests to initialize the MDC (Mapped Diagnostic Context)
 * for distributed tracing and centralized logging.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Extract Trace ID:</strong> Retrieves {@code X-Trace-Id} from headers. If absent,
 *       generates a new UUID.
 *   <li><strong>Bind Trace Context:</strong> Puts the trace ID into the MDC context and adds it to
 *       the HTTP response header.
 *   <li><strong>Extract Client IP:</strong> Resolves the client's actual IP address using standard
 *       proxy headers ({@code X-Client-IP}, {@code X-Forwarded-For}) or the remote address.
 *   <li><strong>Extract Actor ID:</strong> Retrieves the authenticated user's ID from {@code
 *       X-User-Id} (typically passed by the API Gateway).
 *   <li><strong>Clean Up:</strong> Ensures the MDC is completely cleared in the {@code finally}
 *       block to prevent thread-pool memory leaks.
 * </ol>
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // 1. Trace ID
      String traceId = request.getHeader(AppConstant.TRACE_ID_HEADER);
      if (traceId == null || traceId.isEmpty()) {
        traceId = UUID.randomUUID().toString();
      }
      MDC.put(AppConstant.TRACE_ID_KEY, traceId);
      response.addHeader(AppConstant.TRACE_ID_HEADER, traceId);

      // 2. Client IP
      String clientIp = request.getHeader(AuditConstant.HEADER_X_CLIENT_IP);
      if (clientIp == null || clientIp.isEmpty()) {
        clientIp = request.getHeader(AuditConstant.HEADER_X_FORWARDED_FOR);
      }
      if (clientIp == null || clientIp.isEmpty()) {
        clientIp = request.getRemoteAddr();
      }
      MDC.put(AuditConstant.MDC_KEY_CLIENT_IP, clientIp);

      // 3. User ID (from Gateway if authenticated)
      String userId = request.getHeader(AuditConstant.HEADER_X_USER_ID);
      if (userId != null && !userId.isEmpty()) {
        MDC.put(AuditConstant.MDC_KEY_ACTOR_ID, userId);
      }

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear(); // Always clear MDC to prevent memory leaks in thread pools
    }
  }
}

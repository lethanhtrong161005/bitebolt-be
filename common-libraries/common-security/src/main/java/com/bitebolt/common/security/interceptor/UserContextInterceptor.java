package com.bitebolt.common.security.interceptor;

import com.bitebolt.common.security.context.UserContext;
import com.bitebolt.common.security.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Intercepts incoming HTTP requests to extract User Identity headers injected by the API Gateway,
 * populating the ThreadLocal UserContextHolder.
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

  private static final String HEADER_USER_ID = "X-User-Id";
  private static final String HEADER_USER_ROLE = "X-User-Role";
  private static final String HEADER_USER_FULL_NAME = "X-User-FullName";
  private static final String HEADER_USER_EMAIL = "X-User-Email";
  private static final String HEADER_USER_AVATAR = "X-User-Avatar";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String userId = request.getHeader(HEADER_USER_ID);
    String role = request.getHeader(HEADER_USER_ROLE);
    String fullName = decodeHeader(request.getHeader(HEADER_USER_FULL_NAME));
    String email = request.getHeader(HEADER_USER_EMAIL);
    String avatar = decodeHeader(request.getHeader(HEADER_USER_AVATAR));

    if (StringUtils.hasText(userId)) {
      UserContext context =
          UserContext.builder()
              .userId(userId)
              .fullName(fullName)
              .email(email)
              .avatar(avatar)
              .role(role)
              .build();
      UserContextHolder.setContext(context);
      log.debug("UserContext initialized for User ID: {}, Role: {}", userId, role);
    }

    return true;
  }

  private String decodeHeader(String headerValue) {
    if (!StringUtils.hasText(headerValue)) return headerValue;
    try {
      return URLDecoder.decode(headerValue, StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.warn("Failed to decode header value: {}", headerValue);
      return headerValue;
    }
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    // Prevent memory leaks in thread pools by clearing the context when the request is complete
    UserContextHolder.clear();
  }
}

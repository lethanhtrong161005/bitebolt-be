package com.bitebolt.common.security.utils;

import com.bitebolt.common.security.context.UserContext;
import com.bitebolt.common.security.context.UserContextHolder;
import com.bitebolt.common.exception.HttpException;
import org.springframework.stereotype.Component;

/**
 * Enterprise Utility class to access the current authenticated user's context from anywhere in the
 * application (Service layer, Entity Listeners, etc.) without needing to pass it down from the
 * Controller via @CurrentUser.
 */
@Component
public class SecurityUtils {

  /**
   * Retrieves the current authenticated UserContext. Throws an exception if the user is not
   * authenticated.
   */
  public static UserContext getCurrentUser() {
    UserContext context = UserContextHolder.getContext();
    if (context == null || context.getUserId() == null) {
      throw new HttpException(401, "ERROR_UNAUTHORIZED");
    }
    return context;
  }

  /**
   * Retrieves the current authenticated UserContext safely. Returns null if the user is not
   * authenticated instead of throwing an exception.
   */
  public static UserContext getCurrentUserOrNull() {
    return UserContextHolder.getContext();
  }

  /** Retrieves only the current authenticated User ID safely. */
  public static String getCurrentUserIdOrNull() {
    UserContext context = getCurrentUserOrNull();
    return context != null ? context.getUserId() : null;
  }
}

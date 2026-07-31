package com.bitebolt.common.security.context;

/**
 * ThreadLocal holder for the authenticated user's context.
 * Ensures that the UserContext is accessible globally within the current HTTP request thread.
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

    public static void setContext(UserContext context) {
        contextHolder.set(context);
    }

    public static UserContext getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}

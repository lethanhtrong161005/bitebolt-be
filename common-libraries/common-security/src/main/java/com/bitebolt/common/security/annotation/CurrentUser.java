package com.bitebolt.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the authenticated user's context into a controller method parameter.
 * 
 * Example:
 * <pre>
 *   public ResponseEntity<?> getProfile(@CurrentUser UserContext context)
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}

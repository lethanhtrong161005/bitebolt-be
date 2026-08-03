package com.bitebolt.common.logging.audit;

import com.bitebolt.common.logging.constant.AuditConstant;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require automatic audit logging.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Annotation:</strong> Developer annotates a business method with
 *       {@code @Auditable(action = ...)}.
 *   <li><strong>Interception:</strong> The {@link AuditAspect} intercepts the method call at
 *       runtime.
 *   <li><strong>Event Generation:</strong> An audit event is generated detailing the action,
 *       resource, and execution status.
 * </ol>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
  AuditAction action();

  String resourceType() default AuditConstant.DEFAULT_RESOURCE_TYPE;

  String resourceIdParam() default ""; // Name of the method parameter containing the resource ID
}

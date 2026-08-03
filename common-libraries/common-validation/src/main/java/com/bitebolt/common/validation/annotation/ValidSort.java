package com.bitebolt.common.validation.annotation;

import com.bitebolt.common.validation.validator.SortConditionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation validating whether requested sort field name is present in allowed fields set.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Annotation:</strong> Annotated on classes extending {@code SortBaseCondition}.
 *   <li><strong>Validation:</strong> Validated by {@link SortConditionValidator} by invoking {@code getAllowedFields()}.
 *   <li><strong>Resolution:</strong> Message template is resolved via Spring {@code MessageSource} for i18n VI/EN output.
 * </ol>
 */
@Documented
@Constraint(validatedBy = SortConditionValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSort
{
  /**
   * Error message code key resolved by MessageSource for i18n localization.
   *
   * @return Error message code key template.
   */
  String message() default "ERROR_INVALID_SORT_FIELD";

  /**
   * Validation groups.
   *
   * @return Group array.
   */
  Class<?>[] groups() default {};

  /**
   * Payload object types.
   *
   * @return Payload array.
   */
  Class<? extends Payload>[] payload() default {};
}

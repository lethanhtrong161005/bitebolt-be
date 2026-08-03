package com.bitebolt.common.validation.annotation;

import com.bitebolt.common.validation.validator.EnumValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation to validate whether a string input value corresponds to an allowed Enum constant.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Annotation:</strong> Annotated on String fields representing Enum names (e.g. sortOrder).
 *   <li><strong>Validation:</strong> Validated by {@link EnumValueValidator} using reflection on target Enum values.
 *   <li><strong>Resolution:</strong> Message template is resolved via Spring {@code MessageSource} for i18n VI/EN output.
 * </ol>
 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue
{
  /**
   * Target Enum class to validate string value against.
   *
   * @return Class of Enum target.
   */
  Class<? extends Enum<?>> enumClass();

  /**
   * Error message code key resolved by MessageSource for i18n localization.
   *
   * @return Message code key template.
   */
  String message() default "ERROR_INVALID_ENUM_VALUE";

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

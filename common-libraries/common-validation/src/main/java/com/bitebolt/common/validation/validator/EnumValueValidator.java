package com.bitebolt.common.validation.validator;

import com.bitebolt.common.validation.annotation.EnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Constraint validator checking if string value matches any valid enum constant name.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Initialization:</strong> Reads enum constants from the target Enum class.
 *   <li><strong>Validation:</strong> Checks if value is null (valid) or present in allowed enum name set (case-insensitive or exact match).
 * </ol>
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, String>
{
  private Set<String> acceptedValues;

  @Override
  public void initialize(EnumValue constraintAnnotation) {
    Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
    acceptedValues = Arrays.stream(enumClass.getEnumConstants())
        .map(Enum::name)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return true;
    }
    return acceptedValues.contains(value.trim().toUpperCase());
  }
}

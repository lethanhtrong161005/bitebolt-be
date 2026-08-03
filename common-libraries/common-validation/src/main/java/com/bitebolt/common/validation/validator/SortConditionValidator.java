package com.bitebolt.common.validation.validator;

import com.bitebolt.common.validation.annotation.ValidSort;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Constraint validator verifying that requested sort field is in allowed sorting fields set.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Inspection:</strong> Reflectively invokes {@code getSortBy()} and {@code getAllowedFields()} on the target object.
 *   <li><strong>Check:</strong> Validates if target field is contained within {@code getAllowedFields()} returned set.
 * </ol>
 */
public class SortConditionValidator implements ConstraintValidator<ValidSort, Object>
{
  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    try {
      Method getSortByMethod = value.getClass().getMethod("getSortBy");
      Method getAllowedFieldsMethod = value.getClass().getMethod("getAllowedFields");

      String sortBy = (String) getSortByMethod.invoke(value);
      if (sortBy == null || sortBy.trim().isEmpty()) {
        return true;
      }

      @SuppressWarnings("unchecked")
      Set<String> allowedFields = (Set<String>) getAllowedFieldsMethod.invoke(value);
      if (allowedFields == null) {
        return false;
      }

      return allowedFields.contains(sortBy.trim());

    } catch (Exception e) {
      return true;
    }
  }
}

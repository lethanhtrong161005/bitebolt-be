package com.bitebolt.common.dto.request;

/**
 * Enumeration representing allowed sort directions.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Specification:</strong> Used in {@link SortBaseCondition} to define sorting direction.
 *   <li><strong>Validation:</strong> Validated by {@code EnumValue} annotation constraint.
 * </ol>
 */
public enum SortOrder
{
  ASC,
  DESC
}

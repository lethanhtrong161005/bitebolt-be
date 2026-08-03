package com.bitebolt.common.dto.request;

import com.bitebolt.common.constant.MessageConstant;
import com.bitebolt.common.validation.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * Base abstract contract class for module-specific sorting condition payloads.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Extension:</strong> Extended by domain specific sort classes (e.g. EmployeeSortCondition).
 *   <li><strong>Validation:</strong> Validated by {@code ValidSort} validator against allowed fields set.
 * </ol>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class SortBaseCondition implements Serializable
{
  private static final long serialVersionUID = 1L;

  /** Target entity attribute field name to sort by. */
  private String sortBy;

  /** Sorting order direction ("ASC" or "DESC"). */
  @EnumValue(enumClass = SortOrder.class, message = MessageConstant.ERROR_INVALID_SORT_ORDER)
  private String sortOrder;

  /**
   * Abstract contract defining set of entity attribute fields allowed for sorting in business module.
   *
   * @return Set of allowed field names.
   */
  @JsonIgnore
  @Schema(hidden = true)
  public abstract Set<String> getAllowedFields();
}

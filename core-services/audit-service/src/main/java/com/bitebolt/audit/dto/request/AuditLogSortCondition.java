package com.bitebolt.audit.dto.request;

import com.bitebolt.common.constant.MessageConstant;
import com.bitebolt.common.dto.request.SortBaseCondition;
import com.bitebolt.common.validation.annotation.ValidSort;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Module specific sort condition payload for audit compliance log searches.
 * Extends {@link SortBaseCondition} and defines allowed sort attributes.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Binding:</strong> Bound within {@code PayloadSearchRequest<AuditLogSortCondition, AuditLogSearchRequest>}.
 *   <li><strong>Validation:</strong> Validated by {@link ValidSort} against {@code getAllowedFields()}.
 * </ol>
 */
@Data
@SuperBuilder
@ValidSort(message = MessageConstant.ERROR_INVALID_SORT_FIELD)
@EqualsAndHashCode(callSuper = true)
public class AuditLogSortCondition extends SortBaseCondition
{
  private static final long serialVersionUID = 1L;

  private static final Set<String> ALLOWED_FIELDS = Set.of(
      "id",
      "traceId",
      "actorId",
      "action",
      "status",
      "service",
      "createdAt"
  );

  public AuditLogSortCondition() {
    super();
  }

  @Hidden
  @Override
  public Set<String> getAllowedFields() {
    return ALLOWED_FIELDS;
  }
}

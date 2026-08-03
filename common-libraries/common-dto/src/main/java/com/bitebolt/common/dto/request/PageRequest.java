package com.bitebolt.common.dto.request;

import com.bitebolt.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Standard page request pagination DTO containing page number and page size criteria.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Binding:</strong> Bound within {@link PayloadSearchRequest} generic envelope.
 *   <li><strong>Validation:</strong> Validates pageNumber and pageSize are positive non-null integers.
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest implements Serializable
{
  private static final long serialVersionUID = 1L;

  /**
   * 1-based page index number. Default is 1.
   */
  @Schema(description = "Page number (starts from 1)", example = "1")
  @NotNull(message = MessageConstant.ERROR_PAGE_NUMBER_REQUIRED)
  @Min(value = 1, message = MessageConstant.ERROR_PAGE_NUMBER_MIN)
  @Builder.Default
  private Integer pageNumber = 1;

  /**
   * Number of items per page. Default is 10.
   */
  @Schema(description = "Page size limit", example = "10")
  @NotNull(message = MessageConstant.ERROR_PAGE_SIZE_REQUIRED)
  @Min(value = 1, message = MessageConstant.ERROR_PAGE_SIZE_MIN)
  @Builder.Default
  private Integer pageSize = 10;
}

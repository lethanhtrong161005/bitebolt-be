package com.bitebolt.common.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Enterprise generic payload search request wrapper encapsulating pagination, sorting criteria, and module-specific search parameters.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Reception:</strong> Received by controller endpoints for search and query APIs.
 *   <li><strong>Validation:</strong> Cascades JSR-380 validation to {@code page}, {@code sort}, and {@code search} payloads.
 * </ol>
 *
 * @param <S> SortBaseCondition sub-class module sorting type.
 * @param <T> Module search request payload type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadSearchRequest<S extends SortBaseCondition, T> implements Serializable
{
  private static final long serialVersionUID = 1L;

  /** Pagination parameters payload. */
  @Valid
  private PageRequest page;

  /** Sorting criteria payload. */
  @Valid
  private S sort;

  /** Module specific search criteria payload. */
  @Valid
  private T search;

  /**
   * Null-safe getter for search criteria payload.
   *
   * <p><strong>Standard Execution Steps:</strong>
   *
   * <ol>
   *   <li><strong>Null Check:</strong> Returns search payload or null.
   * </ol>
   *
   * @return Search criteria payload object.
   */
  public T safeSearch() {
    return this.search;
  }

  /**
   * Null-safe getter for pagination request payload.
   *
   * <p><strong>Standard Execution Steps:</strong>
   *
   * <ol>
   *   <li><strong>Null Check:</strong> Returns page payload or default non-null PageRequest object.
   * </ol>
   *
   * @return PageRequest object.
   */
  public PageRequest safePage() {
    return this.page != null ? this.page : PageRequest.builder().build();
  }

  /**
   * Null-safe getter for sort condition payload.
   *
   * <p><strong>Standard Execution Steps:</strong>
   *
   * <ol>
   *   <li><strong>Null Check:</strong> Returns sort condition payload object.
   * </ol>
   *
   * @return SortBaseCondition payload object.
   */
  public S safeSort() {
    return this.sort;
  }
}

package com.bitebolt.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * Enterprise standard pagination response wrapper envelope for REST APIs.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Mapping:</strong> Built from Spring Data {@link Page} objects using factory methods or service helpers.
 *   <li><strong>Serialization:</strong> Serialized to JSON envelope for consumption by clients.
 * </ol>
 *
 * @param <T> Content payload element type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable
{
  private static final long serialVersionUID = 1L;

  /** List of payload item elements in current page. */
  private List<T> content;

  /** Zero-indexed current page number. */
  private int page;

  /** Number of items requested per page. */
  private int size;

  /** Total number of elements across all pages. */
  private long totalElements;

  /** Total number of pages available. */
  private int totalPages;

  /** Flag indicating if current page is the last page. */
  private boolean last;

  /** Flag indicating if current page is the first page. */
  private boolean first;

  /**
   * Static factory method to convert Spring Data {@link Page} into {@link PageResponse}.
   *
   * @param page Spring Data Page instance. Must be non-null.
   * @param mapper Function mapper converting entity to DTO element.
   * @param <E> Source entity element type.
   * @param <DTO> Target response DTO element type.
   * @return Formatted PageResponse instance envelope.
   */
  public static <E, DTO> PageResponse<DTO> of(Page<E> page, java.util.function.Function<E, DTO> mapper) {
    List<DTO> dtoList = page.getContent().stream().map(mapper).toList();
    return PageResponse.<DTO>builder()
        .content(dtoList)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .first(page.isFirst())
        .build();
  }
}

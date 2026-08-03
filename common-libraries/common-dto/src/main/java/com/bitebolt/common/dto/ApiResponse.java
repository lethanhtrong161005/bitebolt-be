package com.bitebolt.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Generic Data Transfer Object (DTO) representing a standardized API response.
 *
 * <p>This class defines a consistent response structure for all REST APIs across the bitebolt
 * platform. It encapsulates:
 *
 * <ul>
 *   <li>HTTP status code
 *   <li>Localized response message
 *   <li>Response payload
 *   <li>Request trace identifier for distributed tracing
 *   <li>Response timestamp
 * </ul>
 *
 * <p>Using a unified response format simplifies client-side processing, improves API consistency,
 * and facilitates logging and debugging in a microservices environment.
 *
 * <p>Example response:
 *
 * <pre>{@code
 * {
 *   "status": 200,
 *   "message": {
 *     "code": "USER_CREATED",
 *     "vi": "Tạo người dùng thành công.",
 *     "en": "User created successfully."
 *   },
 *   "data": {
 *     "id": 1,
 *     "name": "John Doe"
 *   },
 *   "traceRequest": "4abf9d2a0e8142cb",
 *   "time": "2026-07-16T15:20:45.123Z"
 * }
 * }</pre>
 *
 * @param <T> the type of the response payload
 * @author bitebolt Team
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  /** HTTP status code of the response. */
  private int status;

  /** Localized response message. */
  private LocalizedMessageDto message;

  /**
   * Response payload.
   *
   * <p>May be {@code null} for responses without content or when an error occurs.
   */
  private T data;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<LocalizedMessageDto> errors;

  /** Distributed tracing identifier used to correlate logs across multiple services. */
  private String traceRequest;

  /** Timestamp indicating when the response was generated. */
  private Instant time;
}

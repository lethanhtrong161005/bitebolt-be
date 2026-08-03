package com.bitebolt.audit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.Instant;

/**
 * Module specific search request payload containing filter criteria parameters for audit log query APIs.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Binding:</strong> Bound inside generic {@code SearchRequest<AuditLogSortCondition, AuditLogSearchRequest>}.
 *   <li><strong>Criteria Building:</strong> Used in service layer to construct JPA specifications.
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchRequest implements Serializable
{
  private static final long serialVersionUID = 1L;

  /** Optional trace UUID filter. */
  @Schema(description = "Trace UUID identifier")
  private String traceId;

  /** Optional actor UUID identifier filter. */
  @Schema(description = "Actor UUID identifier")
  private String actorId;

  /** Optional actor email filter. */
  @Schema(description = "Actor email address")
  private String actorEmail;

  /** Optional actor full name filter. */
  @Schema(description = "Actor full name")
  private String actorName;

  /** Optional audit action filter. */
  @Schema(description = "Audit action name")
  private String action;

  /** Optional execution status filter (SUCCESS/FAILURE). */
  @Schema(description = "Audit event execution status")
  private String status;

  /** Optional start date range boundary. */
  @Schema(description = "Start timestamp ISO date boundary")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant startDate;

  /** Optional end date range boundary. */
  @Schema(description = "End timestamp ISO date boundary")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant endDate;
}

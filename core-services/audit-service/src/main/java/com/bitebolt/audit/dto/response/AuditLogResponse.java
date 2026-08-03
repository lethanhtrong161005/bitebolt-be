package com.bitebolt.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object representing an individual audit event response.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Instantiation:</strong> Mapped from {@code AuditLog} entity in {@code AuditQueryServiceImpl}.
 *   <li><strong>Serialization:</strong> Serialized to JSON envelope for frontend Audit Logs UI consumption.
 * </ol>
 *
 * @see com.bitebolt.audit.entity.AuditLog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse
{
  /** Unique audit record identifier. */
  private Long id;

  /** Distributed correlation trace UUID. */
  private String traceId;

  /** User email or system identifier performing the action. */
  private String actorId;

  /** Immutable snapshot of actor email at event time. */
  private String actorEmail;

  /** Immutable snapshot of actor full name at event time. */
  private String actorName;

  /** Client IP address recorded during the request. */
  private String actorIp;

  /** Standardized enterprise audit action. */
  private String action;

  /** Target resource type category. */
  private String resourceType;

  /** Target resource identifier. */
  private String resourceId;

  /** Execution outcome status (SUCCESS/FAILURE). */
  private String status;

  /** Originating microservice application name. */
  private String service;

  /** Extended contextual JSON payload. */
  private String details;

  /** ISO-8601 timestamp when audit event was recorded. */
  private Instant createdAt;
}

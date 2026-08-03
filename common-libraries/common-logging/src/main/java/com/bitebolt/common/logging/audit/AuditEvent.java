package com.bitebolt.common.logging.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object representing a system audit event.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Creation:</strong> Instantiated by the {@link AuditAspect} using the builder pattern.</li>
 *   <li><strong>Population:</strong> Filled with metadata from MDC (traceId, actorId) and method context.</li>
 *   <li><strong>Transfer:</strong> Serialized to JSON and sent to the Kafka broker.</li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String logType;
    private String traceId;
    private String actorId;
    private String actorIp;
    private String action;
    private String resourceType;
    private String resourceId;
    private String status;
    private String service;
    private String details;
    private Instant timestamp;
}

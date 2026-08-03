package com.bitebolt.audit.listener;

import com.bitebolt.audit.entity.AuditLog;
import com.bitebolt.audit.repository.AuditLogRepository;
import com.bitebolt.common.logging.audit.AuditEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener responsible for consuming audit events and persisting them to the database.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Listen:</strong> Subscribes to the {@code audit.events} Kafka topic.
 *   <li><strong>Deserialize:</strong> Parses the JSON payload into an {@link AuditEvent} object.
 *   <li><strong>Map:</strong> Converts the DTO into an {@link AuditLog} entity.
 *   <li><strong>Persist:</strong> Saves the entity to the database via {@link AuditLogRepository}.
 *   <li><strong>Error Handling:</strong> Logs errors gracefully if JSON parsing or database saving
 *       fails.
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditKafkaListener {

  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "audit.events", groupId = "audit-service-group")
  public void handleAuditEvent(String payload) {
    try {
      AuditEvent event = objectMapper.readValue(payload, AuditEvent.class);
      AuditLog auditLog =
          AuditLog.builder()
              .traceId(event.getTraceId())
              .actorId(event.getActorId())
              .actorIp(event.getActorIp())
              .action(event.getAction())
              .resourceType(event.getResourceType())
              .resourceId(event.getResourceId())
              .status(event.getStatus())
              .service(event.getService())
              .details(event.getDetails())
              .build();

      auditLogRepository.save(auditLog);
      log.info("Saved audit event: traceId={}, action={}", event.getTraceId(), event.getAction());
    } catch (JsonProcessingException e) {
      log.error("Failed to parse audit event payload: {}", payload, e);
    } catch (Exception e) {
      log.error("Failed to save audit event", e);
    }
  }
}

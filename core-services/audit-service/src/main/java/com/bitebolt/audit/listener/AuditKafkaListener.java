package com.bitebolt.audit.listener;

import com.bitebolt.audit.document.AuditLogDocument;
import com.bitebolt.audit.entity.AuditLog;
import com.bitebolt.audit.repository.AuditLogElasticsearchRepository;
import com.bitebolt.audit.repository.AuditLogRepository;
import com.bitebolt.common.logging.audit.AuditEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Kafka listener responsible for consuming audit events, persisting them to PostgreSQL,
 * and dual-writing immutable event snapshots to Elasticsearch.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditKafkaListener {

  private final AuditLogRepository auditLogRepository;
  private final AuditLogElasticsearchRepository auditLogElasticsearchRepository;
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

      AuditLog savedLog = auditLogRepository.save(auditLog);

      // Index into Elasticsearch for high-performance Full-Text Dual-Search
      try {
        AuditLogDocument document =
            AuditLogDocument.builder()
                .id(savedLog.getId() != null ? savedLog.getId().toString() : null)
                .traceId(event.getTraceId())
                .actorId(event.getActorId())
                .actorEmail(event.getActorEmail())
                .actorName(event.getActorName())
                .action(event.getAction())
                .status(event.getStatus())
                .service(event.getService())
                .actorIp(event.getActorIp())
                .resourceType(event.getResourceType())
                .resourceId(event.getResourceId())
                .details(event.getDetails())
                .createdAt(savedLog.getCreatedAt() != null ? savedLog.getCreatedAt() : Instant.now())
                .build();

        auditLogElasticsearchRepository.save(document);
      } catch (Exception esEx) {
        log.warn("Failed to index audit log into Elasticsearch (falling back to DB): {}", esEx.getMessage());
      }

      log.info("Saved audit event: traceId={}, action={}", event.getTraceId(), event.getAction());
    } catch (JsonProcessingException e) {
      log.error("Failed to parse audit event payload: {}", payload, e);
    } catch (Exception e) {
      log.error("Failed to save audit event", e);
    }
  }
}


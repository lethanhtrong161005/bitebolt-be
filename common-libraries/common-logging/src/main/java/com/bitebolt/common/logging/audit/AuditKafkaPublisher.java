package com.bitebolt.common.logging.audit;

import com.bitebolt.common.logging.constant.AuditConstant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher responsible for asynchronously sending audit events to the Kafka broker.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Serialize:</strong> Converts the {@link AuditEvent} object into a JSON string using
 *       {@link ObjectMapper}.
 *   <li><strong>Publish:</strong> Sends the JSON payload to the {@code audit.events} topic using
 *       the trace ID as the message key.
 *   <li><strong>Async Callback:</strong> Attaches a non-blocking callback ({@code whenComplete}) to
 *       log success or failure.
 *   <li><strong>Exception Handling:</strong> Catches and logs any JSON serialization errors without
 *       throwing them.
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditKafkaPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void publish(AuditEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      // Fire and forget
      kafkaTemplate
          .send(AuditConstant.TOPIC_AUDIT_EVENTS, event.getTraceId(), payload)
          .whenComplete(
              (result, ex) -> {
                if (ex != null) {
                  log.error("Failed to publish audit event to Kafka: {}", ex.getMessage(), ex);
                }
              });
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize audit event", e);
    }
  }
}

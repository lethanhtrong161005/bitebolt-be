package com.bitebolt.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerHelper {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public <T> void sendEvent(String topic, String key, T event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate
          .send(topic, key, payload)
          .whenComplete(
              (result, ex) -> {
                if (ex != null) {
                  log.error("Failed to publish event to Kafka. Topic: {}, Key: {}", topic, key, ex);
                } else {
                  log.info(
                      "Successfully published event to Kafka. Topic: {}, Partition: {}, Offset: {}",
                      result.getRecordMetadata().topic(),
                      result.getRecordMetadata().partition(),
                      result.getRecordMetadata().offset());
                }
              });
    } catch (Exception e) {
      log.error("Failed to serialize and send event to Kafka. Topic: {}", topic, e);
    }
  }

  public <T> void sendEvent(String topic, T event) {
    sendEvent(topic, null, event);
  }
}

package com.bitebolt.common.logging.logger;

import com.bitebolt.common.logging.constant.AuditConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for logging Kafka message production and consumption.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Set Context:</strong> Injects {@code log_type}, {@code direction}
 *       (PRODUCE/CONSUME), {@code topic}, and metadata into MDC.
 *   <li><strong>Log:</strong> Uses SLF4J to log an ERROR if the event failed, or INFO if
 *       successful.
 *   <li><strong>Clean Up:</strong> Removes the injected metrics from MDC to avoid context
 *       pollution.
 * </ol>
 */
@Slf4j
public class KafkaLogger {

  private KafkaLogger() {}

  public static void logEvent(
      String direction,
      String topic,
      String key,
      Integer partition,
      Long offset,
      String status,
      String errorMessage) {
    MDC.put(AuditConstant.MDC_KEY_LOG_TYPE, AuditConstant.LOG_TYPE_KAFKA);
    MDC.put(AuditConstant.MDC_KEY_DIRECTION, direction); // PRODUCE or CONSUME
    MDC.put(AuditConstant.MDC_KEY_TOPIC, topic);
    if (key != null) MDC.put(AuditConstant.MDC_KEY_KEY, key);
    if (partition != null) MDC.put(AuditConstant.MDC_KEY_PARTITION, String.valueOf(partition));
    if (offset != null) MDC.put(AuditConstant.MDC_KEY_OFFSET, String.valueOf(offset));
    MDC.put(AuditConstant.MDC_KEY_STATUS, status);

    if (AuditConstant.STATUS_SUCCESS.equalsIgnoreCase(status)) {
      log.info(
          "Kafka {}: [{}] key={} partition={} offset={}", direction, topic, key, partition, offset);
    } else {
      log.error("Kafka {}: [{}] key={} failed. Error: {}", direction, topic, key, errorMessage);
    }

    MDC.remove(AuditConstant.MDC_KEY_LOG_TYPE);
    MDC.remove(AuditConstant.MDC_KEY_DIRECTION);
    MDC.remove(AuditConstant.MDC_KEY_TOPIC);
    MDC.remove(AuditConstant.MDC_KEY_KEY);
    MDC.remove(AuditConstant.MDC_KEY_PARTITION);
    MDC.remove(AuditConstant.MDC_KEY_OFFSET);
    MDC.remove(AuditConstant.MDC_KEY_STATUS);
  }

  public static void logProduceSuccess(String topic, String key, int partition, long offset) {
    logEvent("PRODUCE", topic, key, partition, offset, AuditConstant.STATUS_SUCCESS, null);
  }

  public static void logProduceFailure(String topic, String key, String errorMessage) {
    logEvent("PRODUCE", topic, key, null, null, AuditConstant.STATUS_FAILURE, errorMessage);
  }

  public static void logConsumeSuccess(String topic, String key, int partition, long offset) {
    logEvent("CONSUME", topic, key, partition, offset, AuditConstant.STATUS_SUCCESS, null);
  }
}

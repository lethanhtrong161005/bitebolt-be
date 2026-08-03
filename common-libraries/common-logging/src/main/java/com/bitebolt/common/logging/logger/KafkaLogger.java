package com.bitebolt.common.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for logging Kafka message production and consumption.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Set Context:</strong> Injects {@code log_type}, {@code direction} (PRODUCE/CONSUME), {@code topic}, and metadata into MDC.</li>
 *   <li><strong>Log:</strong> Uses SLF4J to log an ERROR if the event failed, or INFO if successful.</li>
 *   <li><strong>Clean Up:</strong> Removes the injected metrics from MDC to avoid context pollution.</li>
 * </ol>
 */
@Slf4j
public class KafkaLogger {

    private KafkaLogger() {
    }

    public static void logEvent(String direction, String topic, String key, Integer partition, Long offset, String status, String errorMessage) {
        MDC.put("log_type", "KAFKA");
        MDC.put("direction", direction); // PRODUCE or CONSUME
        MDC.put("topic", topic);
        if (key != null) MDC.put("key", key);
        if (partition != null) MDC.put("partition", String.valueOf(partition));
        if (offset != null) MDC.put("offset", String.valueOf(offset));
        MDC.put("status", status);

        if ("SUCCESS".equalsIgnoreCase(status)) {
            log.info("Kafka {}: [{}] key={} partition={} offset={}", direction, topic, key, partition, offset);
        } else {
            log.error("Kafka {}: [{}] key={} failed. Error: {}", direction, topic, key, errorMessage);
        }

        MDC.remove("log_type");
        MDC.remove("direction");
        MDC.remove("topic");
        MDC.remove("key");
        MDC.remove("partition");
        MDC.remove("offset");
        MDC.remove("status");
    }

    public static void logProduceSuccess(String topic, String key, int partition, long offset) {
        logEvent("PRODUCE", topic, key, partition, offset, "SUCCESS", null);
    }

    public static void logProduceFailure(String topic, String key, String errorMessage) {
        logEvent("PRODUCE", topic, key, null, null, "FAILURE", errorMessage);
    }

    public static void logConsumeSuccess(String topic, String key, int partition, long offset) {
        logEvent("CONSUME", topic, key, partition, offset, "SUCCESS", null);
    }
}

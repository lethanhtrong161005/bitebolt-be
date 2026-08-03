package com.bitebolt.common.logging.logger;

import com.bitebolt.common.logging.constant.AuditConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for performance monitoring, specifically tracking slow operations.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Evaluate Threshold:</strong> Checks if the duration exceeds the defined threshold.
 *   <li><strong>Set Context:</strong> Injects performance metrics (duration, threshold) into MDC if
 *       slow.
 *   <li><strong>Log:</strong> Uses SLF4J to log a WARN message.
 *   <li><strong>Clean Up:</strong> Removes the injected metrics from MDC.
 * </ol>
 */
@Slf4j
public class PerformanceLogger {

  private PerformanceLogger() {}

  public static void logSlowOperation(String operation, long durationMs, long thresholdMs) {
    if (durationMs > thresholdMs) {
      MDC.put(AuditConstant.MDC_KEY_LOG_TYPE, AuditConstant.LOG_TYPE_PERFORMANCE);
      MDC.put(AuditConstant.MDC_KEY_OPERATION, operation);
      MDC.put(AuditConstant.MDC_KEY_DURATION_MS, String.valueOf(durationMs));
      MDC.put(AuditConstant.MDC_KEY_THRESHOLD_MS, String.valueOf(thresholdMs));
      MDC.put(AuditConstant.MDC_KEY_IS_SLOW, "true");

      log.warn(
          "Slow Performance Detected: {} took {}ms (threshold: {}ms)",
          operation,
          durationMs,
          thresholdMs);

      MDC.remove(AuditConstant.MDC_KEY_LOG_TYPE);
      MDC.remove(AuditConstant.MDC_KEY_OPERATION);
      MDC.remove(AuditConstant.MDC_KEY_DURATION_MS);
      MDC.remove(AuditConstant.MDC_KEY_THRESHOLD_MS);
      MDC.remove(AuditConstant.MDC_KEY_IS_SLOW);
    }
  }
}

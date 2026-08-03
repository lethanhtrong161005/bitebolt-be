package com.bitebolt.common.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for performance monitoring, specifically tracking slow operations.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Evaluate Threshold:</strong> Checks if the duration exceeds the defined threshold.</li>
 *   <li><strong>Set Context:</strong> Injects performance metrics (duration, threshold) into MDC if slow.</li>
 *   <li><strong>Log:</strong> Uses SLF4J to log a WARN message.</li>
 *   <li><strong>Clean Up:</strong> Removes the injected metrics from MDC.</li>
 * </ol>
 */
@Slf4j
public class PerformanceLogger {

    private PerformanceLogger() {
    }

    public static void logSlowOperation(String operation, long durationMs, long thresholdMs) {
        if (durationMs > thresholdMs) {
            MDC.put("log_type", "PERFORMANCE");
            MDC.put("operation", operation);
            MDC.put("duration_ms", String.valueOf(durationMs));
            MDC.put("threshold_ms", String.valueOf(thresholdMs));
            MDC.put("is_slow", "true");

            log.warn("Slow Performance Detected: {} took {}ms (threshold: {}ms)", operation, durationMs, thresholdMs);

            MDC.remove("log_type");
            MDC.remove("operation");
            MDC.remove("duration_ms");
            MDC.remove("threshold_ms");
            MDC.remove("is_slow");
        }
    }
}

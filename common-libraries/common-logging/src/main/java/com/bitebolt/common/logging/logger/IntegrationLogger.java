package com.bitebolt.common.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for logging external or cross-service integration calls (e.g., HTTP, gRPC).
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Set Context:</strong> Injects {@code log_type}, {@code protocol}, {@code target_service}, {@code method}, {@code duration_ms}, and {@code status} into MDC.</li>
 *   <li><strong>Log:</strong> Uses SLF4J to log a WARN message if the status is not successful, or INFO if it is.</li>
 *   <li><strong>Clean Up:</strong> Removes the injected metrics from MDC to avoid context pollution.</li>
 * </ol>
 */
@Slf4j
public class IntegrationLogger {

    private IntegrationLogger() {
    }

    public static void logCall(String protocol, String targetService, String method, long durationMs, String status) {
        MDC.put("log_type", "INTEGRATION");
        MDC.put("protocol", protocol);
        MDC.put("target_service", targetService);
        MDC.put("method", method);
        MDC.put("duration_ms", String.valueOf(durationMs));
        MDC.put("status", status);

        if ("OK".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
            log.info("Integration Call: {} to {}.{} completed in {}ms", protocol, targetService, method, durationMs);
        } else {
            log.warn("Integration Call: {} to {}.{} failed with status {} in {}ms", protocol, targetService, method, status, durationMs);
        }

        MDC.remove("log_type");
        MDC.remove("protocol");
        MDC.remove("target_service");
        MDC.remove("method");
        MDC.remove("duration_ms");
        MDC.remove("status");
    }

    public static void grpcCall(String targetService, String method, long durationMs, String status) {
        logCall("gRPC", targetService, method, durationMs, status);
    }
}

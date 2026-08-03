package com.bitebolt.common.logging.logger;

import com.bitebolt.common.logging.constant.AuditConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for logging external or cross-service integration calls (e.g., HTTP, gRPC).
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Set Context:</strong> Injects {@code log_type}, {@code protocol}, {@code
 *       target_service}, {@code method}, {@code duration_ms}, and {@code status} into MDC.
 *   <li><strong>Log:</strong> Uses SLF4J to log a WARN message if the status is not successful, or
 *       INFO if it is.
 *   <li><strong>Clean Up:</strong> Removes the injected metrics from MDC to avoid context
 *       pollution.
 * </ol>
 */
@Slf4j
public class IntegrationLogger {

  private IntegrationLogger() {}

  public static void logCall(
      String protocol, String targetService, String method, long durationMs, String status) {
    MDC.put(AuditConstant.MDC_KEY_LOG_TYPE, AuditConstant.LOG_TYPE_INTEGRATION);
    MDC.put(AuditConstant.MDC_KEY_PROTOCOL, protocol);
    MDC.put(AuditConstant.MDC_KEY_TARGET_SERVICE, targetService);
    MDC.put(AuditConstant.MDC_KEY_METHOD, method);
    MDC.put(AuditConstant.MDC_KEY_DURATION_MS, String.valueOf(durationMs));
    MDC.put(AuditConstant.MDC_KEY_STATUS, status);

    if (AuditConstant.STATUS_OK.equalsIgnoreCase(status)
        || AuditConstant.STATUS_SUCCESS.equalsIgnoreCase(status)) {
      log.info(
          "Integration Call: {} to {}.{} completed in {}ms",
          protocol,
          targetService,
          method,
          durationMs);
    } else {
      log.warn(
          "Integration Call: {} to {}.{} failed with status {} in {}ms",
          protocol,
          targetService,
          method,
          status,
          durationMs);
    }

    MDC.remove(AuditConstant.MDC_KEY_LOG_TYPE);
    MDC.remove(AuditConstant.MDC_KEY_PROTOCOL);
    MDC.remove(AuditConstant.MDC_KEY_TARGET_SERVICE);
    MDC.remove(AuditConstant.MDC_KEY_METHOD);
    MDC.remove(AuditConstant.MDC_KEY_DURATION_MS);
    MDC.remove(AuditConstant.MDC_KEY_STATUS);
  }

  public static void grpcCall(String targetService, String method, long durationMs, String status) {
    logCall("gRPC", targetService, method, durationMs, status);
  }
}

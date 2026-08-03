package com.bitebolt.common.logging.logger;

import com.bitebolt.common.logging.constant.AuditConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for security-related events like access denial, token blacklisting, and
 * brute-force detection.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Set Context:</strong> Injects {@code log_type}, {@code event}, and {@code severity}
 *       into MDC.
 *   <li><strong>Log:</strong> Uses SLF4J to log at an appropriate level (WARN for HIGH/CRITICAL,
 *       INFO for others).
 *   <li><strong>Clean Up:</strong> Removes the injected keys from MDC to prevent context leakage.
 * </ol>
 */
@Slf4j
public class SecurityLogger {

  private SecurityLogger() {
    // Utility class
  }

  public static void logEvent(String event, String severity, String details, String path) {
    MDC.put(AuditConstant.MDC_KEY_LOG_TYPE, AuditConstant.LOG_TYPE_SECURITY);
    MDC.put(AuditConstant.MDC_KEY_EVENT, event);
    MDC.put(AuditConstant.MDC_KEY_SEVERITY, severity);
    if (path != null) {
      MDC.put(AuditConstant.MDC_KEY_PATH, path);
    }

    if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
      log.warn("Security Event: {} - {}", event, details);
    } else {
      log.info("Security Event: {} - {}", event, details);
    }

    MDC.remove(AuditConstant.MDC_KEY_LOG_TYPE);
    MDC.remove(AuditConstant.MDC_KEY_EVENT);
    MDC.remove(AuditConstant.MDC_KEY_SEVERITY);
    if (path != null) {
      MDC.remove(AuditConstant.MDC_KEY_PATH);
    }
  }

  public static void tokenBlacklisted(String path, String details) {
    logEvent("TOKEN_BLACKLISTED", "HIGH", details, path);
  }

  public static void accessDenied(String path, String details) {
    logEvent("ACCESS_DENIED", "MEDIUM", details, path);
  }

  public static void bruteForceDetected(String path, String details) {
    logEvent("BRUTE_FORCE_DETECTED", "CRITICAL", details, path);
  }
}

package com.bitebolt.common.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Utility logger for security-related events like access denial, token blacklisting, and brute-force detection.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Set Context:</strong> Injects {@code log_type}, {@code event}, and {@code severity} into MDC.</li>
 *   <li><strong>Log:</strong> Uses SLF4J to log at an appropriate level (WARN for HIGH/CRITICAL, INFO for others).</li>
 *   <li><strong>Clean Up:</strong> Removes the injected keys from MDC to prevent context leakage.</li>
 * </ol>
 */
@Slf4j
public class SecurityLogger {

    private SecurityLogger() {
        // Utility class
    }

    public static void logEvent(String event, String severity, String details, String path) {
        MDC.put("log_type", "SECURITY");
        MDC.put("event", event);
        MDC.put("severity", severity);
        if (path != null) {
            MDC.put("path", path);
        }

        if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
            log.warn("Security Event: {} - {}", event, details);
        } else {
            log.info("Security Event: {} - {}", event, details);
        }

        MDC.remove("log_type");
        MDC.remove("event");
        MDC.remove("severity");
        if (path != null) {
            MDC.remove("path");
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

package com.bitebolt.common.logging.constant;

/**
 * Utility class holding system-wide constants for logging, auditing, MDC diagnostic context keys,
 * and Kafka audit topic specifications.
 *
 * <p>Follows Google Java Style and BiteBolt Enterprise Coding Standards.
 */
public final class AuditConstant {

  private AuditConstant() {
    // Utility class
  }

  // --- Audit Log Types ---
  public static final String LOG_TYPE_AUDIT = "AUDIT";
  public static final String LOG_TYPE_SECURITY = "SECURITY";
  public static final String LOG_TYPE_INTEGRATION = "INTEGRATION";
  public static final String LOG_TYPE_KAFKA = "KAFKA";
  public static final String LOG_TYPE_PERFORMANCE = "PERFORMANCE";
  public static final String LOG_TYPE_REQUEST = "REQUEST";
  public static final String LOG_TYPE_RESPONSE = "RESPONSE";

  // --- Audit Execution Statuses ---
  public static final String STATUS_SUCCESS = "SUCCESS";
  public static final String STATUS_FAILURE = "FAILURE";
  public static final String STATUS_OK = "OK";

  // --- Default Resource & Fallback Values ---
  public static final String DEFAULT_RESOURCE_TYPE = "System";
  public static final String UNKNOWN_EVAL_ERROR = "unknown-eval-error";
  public static final String UNKNOWN_SERVICE = "unknown-service";

  // --- Kafka Topic Specifications ---
  public static final String TOPIC_AUDIT_EVENTS = "audit.events";

  // --- MDC (Mapped Diagnostic Context) Field Keys ---
  public static final String MDC_KEY_LOG_TYPE = "log_type";
  public static final String MDC_KEY_ACTOR_ID = "actor_id";
  public static final String MDC_KEY_ACTOR_EMAIL = "actor_email";
  public static final String MDC_KEY_ACTOR_NAME = "actor_name";
  public static final String MDC_KEY_CLIENT_IP = "client_ip";
  public static final String MDC_KEY_EVENT = "event";
  public static final String MDC_KEY_SEVERITY = "severity";
  public static final String MDC_KEY_PATH = "path";
  public static final String MDC_KEY_PROTOCOL = "protocol";
  public static final String MDC_KEY_TARGET_SERVICE = "target_service";
  public static final String MDC_KEY_METHOD = "method";
  public static final String MDC_KEY_DURATION_MS = "duration_ms";
  public static final String MDC_KEY_STATUS = "status";
  public static final String MDC_KEY_DIRECTION = "direction";
  public static final String MDC_KEY_TOPIC = "topic";
  public static final String MDC_KEY_KEY = "key";
  public static final String MDC_KEY_PARTITION = "partition";
  public static final String MDC_KEY_OFFSET = "offset";
  public static final String MDC_KEY_OPERATION = "operation";
  public static final String MDC_KEY_THRESHOLD_MS = "threshold_ms";
  public static final String MDC_KEY_IS_SLOW = "is_slow";

  // --- HTTP Headers ---
  public static final String HEADER_X_CLIENT_IP = "X-Client-IP";
  public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String HEADER_X_USER_ID = "X-User-Id";
}

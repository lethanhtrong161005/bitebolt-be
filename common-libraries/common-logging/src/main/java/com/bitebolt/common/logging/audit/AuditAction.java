package com.bitebolt.common.logging.audit;

/**
 * Dictionary of standardized enterprise audit actions.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Selection:</strong> Developer selects an appropriate action from this enum when using {@code @Auditable}.</li>
 *   <li><strong>Serialization:</strong> The action name is serialized into the audit event payload.</li>
 *   <li><strong>Persistence:</strong> The action is saved into the database for future querying and compliance reporting.</li>
 * </ol>
 */
public enum AuditAction {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    SSO_LOGIN_SUCCESS,
    SSO_LOGIN_FAILURE,
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    PASSWORD_CHANGED,
    ROLE_ASSIGNED,
    ROLE_REMOVED,
    CONFIG_UPDATED,
    ORDER_CREATED,
    ORDER_CANCELLED,
    PAYMENT_PROCESSED
}

package com.bitebolt.audit.constant;

/**
 * Message constants for audit-service response payloads.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Resolution:</strong> Resolved by {@code MessageUtils} for i18n localization.
 * </ol>
 */
public final class AuditMessageConstant
{
  private AuditMessageConstant() {
    // Prevent instantiation
  }

  /**
   * <ul>
   *   <li><strong>VI:</strong> Truy vấn nhật ký kiểm toán thành công.
   *   <li><strong>EN:</strong> Audit log query executed successfully.
   * </ul>
   */
  public static final String SUCCESS_AUDIT_LOG_QUERY = "SUCCESS_AUDIT_LOG_QUERY";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Không tìm thấy nhật ký kiểm toán.
   *   <li><strong>EN:</strong> Audit log entry not found.
   * </ul>
   */
  public static final String ERROR_AUDIT_LOG_NOT_FOUND = "ERROR_AUDIT_LOG_NOT_FOUND";
}

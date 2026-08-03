package com.bitebolt.common.constant;

/** Common message constants. */
public final class MessageConstant {

  private MessageConstant() {}

  /**
   *
   *
   * <ul>
   *   <li><strong>VI:</strong> %s là bắt buộc.
   *   <li><strong>EN:</strong> %s is required.
   * </ul>
   */
  public static final String ERROR_REQUIRED_FIELD = "ERROR_REQUIRED_FIELD";

  /**
   *
   *
   * <ul>
   *   <li><strong>VI:</strong> Yêu cầu không hợp lệ.
   *   <li><strong>EN:</strong> Invalid request.
   * </ul>
   */
  public static final String ERROR_BAD_REQUEST = "ERROR_BAD_REQUEST";

  /**
   * ß
   *
   * <ul>
   *   <li><strong>VI:</strong> Yêu cầu xác thực.
   *   <li><strong>EN:</strong> Unauthorized access.
   * </ul>
   */
  public static final String ERROR_UNAUTHORIZED = "ERROR_UNAUTHORIZED";

  /**
   *
   *
   * <ul>
   *   <li><strong>VI:</strong> Không tìm thấy token xác thực.
   *   <li><strong>EN:</strong> Missing authentication token.
   * </ul>
   */
  public static final String ERROR_MISSING_TOKEN = "ERROR_MISSING_TOKEN";

  /**
   *
   *
   * <ul>
   *   <li><strong>VI:</strong> Token không hợp lệ hoặc đã hết hạn.
   *   <li><strong>EN:</strong> Invalid or expired token.
   * </ul>
   */
  public static final String ERROR_INVALID_TOKEN = "ERROR_INVALID_TOKEN";

  /**
   *
   *
   * <ul>
   *   <li><strong>VI:</strong> Token không hợp lệ hoặc đã bị vô hiệu hóa (blacklisted).
   *   <li><strong>EN:</strong> Token is invalid or has been blacklisted.
   * </ul>
   */
  public static final String ERROR_TOKEN_BLACKLISTED = "ERROR_TOKEN_BLACKLISTED";
}

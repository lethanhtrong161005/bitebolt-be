package com.bitebolt.common.constant;

/** Common message constants. */
public final class MessageConstant {

  private MessageConstant() {}

  /**
   * <ul>
   *   <li><strong>VI:</strong> %s là bắt buộc.
   *   <li><strong>EN:</strong> %s is required.
   * </ul>
   */
  public static final String ERROR_REQUIRED_FIELD = "ERROR_REQUIRED_FIELD";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Yêu cầu không hợp lệ.
   *   <li><strong>EN:</strong> Invalid request.
   * </ul>
   */
  public static final String ERROR_BAD_REQUEST = "ERROR_BAD_REQUEST";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Yêu cầu xác thực.
   *   <li><strong>EN:</strong> Unauthorized access.
   * </ul>
   */
  public static final String ERROR_UNAUTHORIZED = "ERROR_UNAUTHORIZED";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Không tìm thấy token xác thực.
   *   <li><strong>EN:</strong> Missing authentication token.
   * </ul>
   */
  public static final String ERROR_MISSING_TOKEN = "ERROR_MISSING_TOKEN";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Token không hợp lệ hoặc đã hết hạn.
   *   <li><strong>EN:</strong> Invalid or expired token.
   * </ul>
   */
  public static final String ERROR_INVALID_TOKEN = "ERROR_INVALID_TOKEN";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Token không hợp lệ hoặc đã bị vô hiệu hóa (blacklisted).
   *   <li><strong>EN:</strong> Token is invalid or has been blacklisted.
   * </ul>
   */
  public static final String ERROR_TOKEN_BLACKLISTED = "ERROR_TOKEN_BLACKLISTED";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Giá trị không hợp lệ cho trường enum.
   *   <li><strong>EN:</strong> Invalid value for enum field.
   * </ul>
   */
  public static final String ERROR_INVALID_ENUM_VALUE = "ERROR_INVALID_ENUM_VALUE";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Trường sắp xếp không hợp lệ.
   *   <li><strong>EN:</strong> Invalid sort field name.
   * </ul>
   */
  public static final String ERROR_INVALID_SORT_FIELD = "ERROR_INVALID_SORT_FIELD";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Chiều sắp xếp phải là 'ASC' hoặc 'DESC'.
   *   <li><strong>EN:</strong> Sort order must be 'ASC' or 'DESC'.
   * </ul>
   */
  public static final String ERROR_INVALID_SORT_ORDER = "ERROR_INVALID_SORT_ORDER";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Số trang không được để trống.
   *   <li><strong>EN:</strong> Page number is required.
   * </ul>
   */
  public static final String ERROR_PAGE_NUMBER_REQUIRED = "ERROR_PAGE_NUMBER_REQUIRED";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Số trang phải lớn hơn 0.
   *   <li><strong>EN:</strong> Page number must be greater than 0.
   * </ul>
   */
  public static final String ERROR_PAGE_NUMBER_MIN = "ERROR_PAGE_NUMBER_MIN";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Kích thước trang không được để trống.
   *   <li><strong>EN:</strong> Page size is required.
   * </ul>
   */
  public static final String ERROR_PAGE_SIZE_REQUIRED = "ERROR_PAGE_SIZE_REQUIRED";

  /**
   * <ul>
   *   <li><strong>VI:</strong> Kích thước trang phải lớn hơn 0.
   *   <li><strong>EN:</strong> Page size must be greater than 0.
   * </ul>
   */
  public static final String ERROR_PAGE_SIZE_MIN = "ERROR_PAGE_SIZE_MIN";
}

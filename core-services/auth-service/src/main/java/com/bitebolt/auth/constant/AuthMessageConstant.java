package com.bitebolt.auth.constant;

/** Authentication message codes. */
public final class AuthMessageConstant {

  private AuthMessageConstant() {
    // Prevent instantiation
  }

  /**
   *
   *
   * <ul>
   *   <li>VI: Số điện thoại hoặc mật khẩu không chính xác.
   *   <li>EN: Invalid phone number or password.
   * </ul>
   */
  public static final String ERROR_UNAUTHORIZED = "ERROR_UNAUTHORIZED";

  /**
   *
   *
   * <ul>
   *   <li>VI: Tài khoản đã bị khóa.
   *   <li>EN: Account is locked.
   * </ul>
   */
  public static final String ERROR_ACCOUNT_LOCKED = "ERROR_ACCOUNT_LOCKED";

  /**
   *
   *
   * <ul>
   *   <li>VI: Mã OTP đã hết hạn hoặc phiên không hợp lệ.
   *   <li>EN: OTP code has expired or session is invalid.
   * </ul>
   */
  public static final String ERROR_OTP_EXPIRED = "ERROR_OTP_EXPIRED";

  /**
   *
   *
   * <ul>
   *   <li>VI: Mã OTP không chính xác.
   *   <li>EN: Invalid OTP code.
   * </ul>
   */
  public static final String ERROR_INVALID_OTP = "ERROR_INVALID_OTP";

  /**
   *
   *
   * <ul>
   *   <li>VI: Phiên bị khóa do nhập sai OTP quá nhiều lần.
   *   <li>EN: Session blocked due to too many failed OTP attempts.
   * </ul>
   */
  public static final String ERROR_TOO_MANY_ATTEMPTS = "ERROR_TOO_MANY_ATTEMPTS";

  /**
   *
   *
   * <ul>
   *   <li>VI: Mã xác thực đã được gửi thành công.
   *   <li>EN: Verification code sent successfully.
   * </ul>
   */
  public static final String SUCCESS_LOGIN_INITIATED = "SUCCESS_LOGIN_INITIATED";

  /**
   *
   *
   * <ul>
   *   <li>VI: Đăng nhập thành công.
   *   <li>EN: Login successful.
   * </ul>
   */
  public static final String SUCCESS_OTP_VERIFIED = "SUCCESS_OTP_VERIFIED";

  /**
   *
   *
   * <ul>
   *   <li>VI: Người dùng không tồn tại.
   *   <li>EN: User not found.
   * </ul>
   */
  public static final String ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND";

  /**
   *
   *
   * <ul>
   *   <li>VI: Hết thời gian kết nối dịch vụ người dùng.
   *   <li>EN: User service request timeout.
   * </ul>
   */
  public static final String ERROR_USER_SERVICE_TIMEOUT = "ERROR_USER_SERVICE_TIMEOUT";

  /**
   *
   *
   * <ul>
   *   <li>VI: Mã người dùng không hợp lệ.
   *   <li>EN: Invalid user ID.
   * </ul>
   */
  public static final String ERROR_INVALID_USER_ID = "ERROR_INVALID_USER_ID";

  /**
   *
   *
   * <ul>
   *   <li>VI: Dịch vụ người dùng không khả dụng.
   *   <li>EN: User service is currently unavailable.
   * </ul>
   */
  public static final String ERROR_USER_SERVICE_UNAVAILABLE = "ERROR_USER_SERVICE_UNAVAILABLE";

  /**
   *
   *
   * <ul>
   *   <li>VI: Vai trò này bắt buộc phải đăng nhập qua Microsoft SSO.
   *   <li>EN: This role must authenticate via Microsoft SSO.
   * </ul>
   */
  public static final String ERROR_SSO_REQUIRED = "ERROR_SSO_REQUIRED";

  /**
   *
   *
   * <ul>
   *   <li>VI: Email không thuộc tên miền nội bộ được chấp nhận.
   *   <li>EN: Email domain is not authorized for internal access.
   * </ul>
   */
  public static final String ERROR_INVALID_EMAIL_DOMAIN = "ERROR_INVALID_EMAIL_DOMAIN";

  /**
   *
   *
   * <ul>
   *   <li>VI: Phiên SSO không hợp lệ hoặc đã hết hạn.
   *   <li>EN: SSO session is invalid or has expired.
   * </ul>
   */
  public static final String ERROR_SSO_STATE_MISMATCH = "ERROR_SSO_STATE_MISMATCH";

  /**
   *
   *
   * <ul>
   *   <li>VI: Đăng nhập SSO thành công.
   *   <li>EN: SSO login successful.
   * </ul>
   */
  public static final String SUCCESS_SSO_LOGIN = "SUCCESS_SSO_LOGIN";

  /**
   *
   *
   * <ul>
   *   <li>VI: Refresh token không hợp lệ hoặc đã hết hạn.
   *   <li>EN: Refresh token is invalid or expired.
   * </ul>
   */
  public static final String ERROR_INVALID_REFRESH_TOKEN = "ERROR_INVALID_REFRESH_TOKEN";

  /**
   *
   *
   * <ul>
   *   <li>VI: Làm mới token thành công.
   *   <li>EN: Token refreshed successfully.
   * </ul>
   */
  public static final String SUCCESS_TOKEN_REFRESHED = "SUCCESS_TOKEN_REFRESHED";

  /**
   *
   *
   * <ul>
   *   <li>VI: Đăng xuất thành công.
   *   <li>EN: Logout successful.
   * </ul>
   */
  public static final String SUCCESS_LOGOUT = "SUCCESS_LOGOUT";

  /**
   *
   *
   * <ul>
   *   <li>VI: Token không hợp lệ hoặc đã bị vô hiệu hóa (Blacklisted).
   *   <li>EN: Token is invalid or has been blacklisted.
   * </ul>
   */
  public static final String ERROR_TOKEN_BLACKLISTED = "ERROR_TOKEN_BLACKLISTED";
}

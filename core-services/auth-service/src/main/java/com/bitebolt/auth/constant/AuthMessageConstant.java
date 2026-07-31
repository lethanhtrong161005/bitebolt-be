package com.bitebolt.auth.constant;

public class AuthMessageConstant {

    /**
     * VI: Số điện thoại hoặc mật khẩu không chính xác.
     * EN: Invalid phone number or password.
     */
    public static final String ERROR_UNAUTHORIZED = "ERROR_UNAUTHORIZED";

    /**
     * VI: Tài khoản đã bị khóa.
     * EN: Account is locked.
     */
    public static final String ERROR_ACCOUNT_LOCKED = "ERROR_ACCOUNT_LOCKED";

    /**
     * VI: Mã OTP đã hết hạn hoặc phiên không hợp lệ.
     * EN: OTP code has expired or session is invalid.
     */
    public static final String ERROR_OTP_EXPIRED = "ERROR_OTP_EXPIRED";

    /**
     * VI: Mã OTP không chính xác.
     * EN: Invalid OTP code.
     */
    public static final String ERROR_INVALID_OTP = "ERROR_INVALID_OTP";

    /**
     * VI: Phiên bị khóa do nhập sai OTP quá nhiều lần.
     * EN: Session blocked due to too many failed OTP attempts.
     */
    public static final String ERROR_TOO_MANY_ATTEMPTS = "ERROR_TOO_MANY_ATTEMPTS";

    /**
     * VI: Mã xác thực đã được gửi thành công.
     * EN: Verification code sent successfully.
     */
    public static final String SUCCESS_LOGIN_INITIATED = "SUCCESS_LOGIN_INITIATED";

    /**
     * VI: Đăng nhập thành công.
     * EN: Login successful.
     */
    public static final String SUCCESS_OTP_VERIFIED = "SUCCESS_OTP_VERIFIED";

    /**
     * VI: Người dùng không tồn tại.
     * EN: User not found.
     */
    public static final String ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND";

    /**
     * VI: Hết thời gian kết nối dịch vụ người dùng.
     * EN: User service request timeout.
     */
    public static final String ERROR_USER_SERVICE_TIMEOUT = "ERROR_USER_SERVICE_TIMEOUT";

    /**
     * VI: Mã người dùng không hợp lệ.
     * EN: Invalid user ID.
     */
    public static final String ERROR_INVALID_USER_ID = "ERROR_INVALID_USER_ID";

    /**
     * VI: Dịch vụ người dùng không khả dụng.
     * EN: User service is currently unavailable.
     */
    public static final String ERROR_USER_SERVICE_UNAVAILABLE = "ERROR_USER_SERVICE_UNAVAILABLE";

    /**
     * VI: Vai trò này bắt buộc phải đăng nhập qua Microsoft SSO.
     * EN: This role must authenticate via Microsoft SSO.
     */
    public static final String ERROR_SSO_REQUIRED = "ERROR_SSO_REQUIRED";

    /**
     * VI: Email không thuộc tên miền nội bộ được chấp nhận.
     * EN: Email domain is not authorized for internal access.
     */
    public static final String ERROR_INVALID_EMAIL_DOMAIN = "ERROR_INVALID_EMAIL_DOMAIN";

    /**
     * VI: Phiên SSO không hợp lệ hoặc đã hết hạn.
     * EN: SSO session is invalid or has expired.
     */
    public static final String ERROR_SSO_STATE_MISMATCH = "ERROR_SSO_STATE_MISMATCH";

    /**
     * VI: Đăng nhập SSO thành công.
     * EN: SSO login successful.
     */
    public static final String SUCCESS_SSO_LOGIN = "SUCCESS_SSO_LOGIN";

    /**
     * VI: Refresh token không hợp lệ hoặc đã hết hạn.
     * EN: Refresh token is invalid or expired.
     */
    public static final String ERROR_INVALID_REFRESH_TOKEN = "ERROR_INVALID_REFRESH_TOKEN";

    /**
     * VI: Làm mới token thành công.
     * EN: Token refreshed successfully.
     */
    public static final String SUCCESS_TOKEN_REFRESHED = "SUCCESS_TOKEN_REFRESHED";

    /**
     * VI: Đăng xuất thành công.
     * EN: Logout successful.
     */
    public static final String SUCCESS_LOGOUT = "SUCCESS_LOGOUT";

    /**
     * VI: Token không hợp lệ hoặc đã bị vô hiệu hóa (Blacklisted).
     * EN: Token is invalid or has been blacklisted.
     */
    public static final String ERROR_TOKEN_BLACKLISTED = "ERROR_TOKEN_BLACKLISTED";
}

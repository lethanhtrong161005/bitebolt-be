package com.bitebolt.common.constant;

public class MessageConstant {

    /**
     * VI: %s là bắt buộc.
     * EN: %s is required.
     */
    public static final String ERROR_REQUIRED_FIELD = "ERROR_REQUIRED_FIELD";

    /**
     * VI: Yêu cầu không hợp lệ.
     * EN: Invalid request.
     */
    public static final String ERROR_BAD_REQUEST = "ERROR_BAD_REQUEST";

    /**
     * VI: Yêu cầu xác thực.
     * EN: Unauthorized Access.
     */
    public static final String ERROR_UNAUTHORIZED = "ERROR_UNAUTHORIZED";

    /**
     * VI: Không tìm thấy Token xác thực.
     * EN: Missing Authentication Token.
     */
    public static final String ERROR_MISSING_TOKEN = "ERROR_MISSING_TOKEN";

    /**
     * VI: Token không hợp lệ hoặc đã hết hạn.
     * EN: Invalid or expired token.
     */
    public static final String ERROR_INVALID_TOKEN = "ERROR_INVALID_TOKEN";

    /**
     * VI: Token không hợp lệ hoặc đã bị vô hiệu hóa (Blacklisted).
     * EN: Token is invalid or has been blacklisted.
     */
    public static final String ERROR_TOKEN_BLACKLISTED = "ERROR_TOKEN_BLACKLISTED";
}

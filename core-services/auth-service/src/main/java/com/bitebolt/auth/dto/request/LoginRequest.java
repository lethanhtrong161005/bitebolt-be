package com.bitebolt.auth.dto.request;

import com.bitebolt.common.constant.MessageConstant;
import com.bitebolt.common.validation.I18nField;
import com.bitebolt.common.validation.RequireField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "LoginRequest",
        description = "Request object used for user authentication"
)
public class LoginRequest {

    @Schema(
            description = "User phone number",
            example = "03560447291",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @RequireField(
            messageCode = MessageConstant.ERROR_REQUIRED_FIELD,
            i18n = @I18nField(
                    vi = "Số điện thoại",
                    en = "Phone number"
            )
    )
    private String phone;

    @Schema(
            description = "User password",
            example = "12345",
            format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @RequireField(
            messageCode = MessageConstant.ERROR_REQUIRED_FIELD,
            i18n = @I18nField(
                    vi = "Mật khẩu",
                    en = "Password"
            )
    )
    private String password;
}
package com.bitebolt.auth.dto.request;

import com.bitebolt.common.constant.MessageConstant;
import com.bitebolt.common.validation.I18nField;
import com.bitebolt.common.validation.RequireField;
import lombok.Data;

@Data
public class LoginRequest {

    @RequireField(
            messageCode = MessageConstant.ERROR_REQUIRED_FIELD,
            i18n = @I18nField(vi = "Số điện thoại", en = "Phone number")
    )
    private String phone;

    @RequireField(
            messageCode = MessageConstant.ERROR_REQUIRED_FIELD,
            i18n = @I18nField(vi = "Mật khẩu", en = "Password")
    )
    private String password;
}

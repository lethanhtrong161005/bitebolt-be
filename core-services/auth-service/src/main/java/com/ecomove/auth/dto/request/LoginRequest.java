package com.ecomove.auth.dto.request;

import com.ecomove.common.constant.MessageConstant;
import com.ecomove.common.validation.I18nField;
import com.ecomove.common.validation.RequireField;
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

package com.ecomove.auth.dto.request;

import com.ecomove.common.constant.MessageConstant;
import com.ecomove.common.validation.I18nField;
import com.ecomove.common.validation.RequireField;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @RequireField(
            messageCode = MessageConstant.ERROR_REQUIRED_FIELD,
            i18n = @I18nField(vi = "Mã phiên", en = "Session ID")
    )
    private String sessionId;

    @RequireField(
            messageCode = MessageConstant.ERROR_REQUIRED_FIELD,
            i18n = @I18nField(vi = "Mã OTP", en = "OTP")
    )
    private String otp;
}

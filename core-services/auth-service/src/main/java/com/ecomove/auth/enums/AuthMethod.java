package com.ecomove.auth.enums;

public enum AuthMethod {
    PHONE_OTP,   // Phone + Password + OTP (DRIVER, RIDER)
    SSO_ENTRA,   // Microsoft Entra ID SSO (ADMIN, STAFF)
    GOOGLE       // Google Social Login (DRIVER, RIDER)
}

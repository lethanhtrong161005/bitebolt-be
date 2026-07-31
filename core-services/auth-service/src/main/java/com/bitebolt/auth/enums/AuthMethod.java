package com.bitebolt.auth.enums;

public enum AuthMethod {
    PHONE_OTP,   // Phone + Password + OTP (SHIPPER, CUSTOMER)
    SSO_ENTRA,   // Microsoft Entra ID SSO (ADMIN, STAFF)
    GOOGLE       // Google Social Login (SHIPPER, CUSTOMER)
}

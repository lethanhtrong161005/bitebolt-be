package com.bitebolt.common.logging.masking;

/**
 * Utility for masking sensitive Personally Identifiable Information (PII) before logging.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Input Check:</strong> Validates if the input string is null or too short to mask.</li>
 *   <li><strong>Masking:</strong> Applies specific string manipulations to obfuscate the middle or end of the data.</li>
 *   <li><strong>Output:</strong> Returns the safely masked string (e.g., {@code 0901***12}, {@code an***@gmail.com}).</li>
 * </ol>
 */
public class MaskingUtil {

    private MaskingUtil() {
        // Utility class
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 2);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return "***@" + domain;
        }
        return name.substring(0, 2) + "***@" + domain;
    }

    public static String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 7) + "...[MASKED]";
    }
}

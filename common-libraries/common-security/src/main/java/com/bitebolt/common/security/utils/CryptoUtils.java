package com.bitebolt.common.security.utils;

import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Enterprise Utility class for Cryptographic operations (hashing, OTP generation).
 * Extracted from Auth Service to be reusable across microservices.
 */
@Slf4j
public class CryptoUtils {

    private static final Random RANDOM = new Random();

    /**
     * Generates a 6-digit One Time Password (OTP).
     *
     * @return 6-digit OTP string
     */
    public static String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Hashes a string (like a token or refresh token) using SHA-256 algorithm.
     * 
     * @param token the raw string to hash
     * @return the SHA-256 hexadecimal hash
     * @throws RuntimeException if the hashing algorithm is missing
     */
    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error occurred while hashing string", e);
            throw new RuntimeException("Failed to hash string", e);
        }
    }
}

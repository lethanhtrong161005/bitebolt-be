package com.ecomove.auth.util;

import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

@Slf4j
public class AuthUtils {

    private static final Random RANDOM = new Random();

    public static String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

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
            log.error("Error occurred while hashing JWT token", e);
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}

package com.autocare.vehicleservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void generateAdminPassword(String rawPassword) {
        String hashedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Generated Hash for '" + rawPassword + "': " + hashedPassword);
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public static void main(String[] args) {
        // 1. Test Verification
        String dbPassword = "$2b$10$yBSi8Q0XQro7MSq/qvig3.4NqPlfWE1JXCvlNoftu/FzfEsJ1w1dS";
        boolean matches = verifyPassword("admin123", dbPassword);
        System.out.println("Does password match? " + matches);

        // 2. Generate a fresh hash for testing
        generateAdminPassword("admin123");
    }
}

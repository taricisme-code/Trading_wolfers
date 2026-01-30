package com.tradingdemo.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtils - Utility class for password hashing and verification
 * Uses BCrypt algorithm for secure password storage
 */
public class PasswordUtils {

    private static final int BCRYPT_ROUNDS = 12;

    /**
     * Generates a hashed password using BCrypt
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain password against a hashed password
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to check against
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Generates a strong password hash for testing
     * @param password The password to hash
     * @return The BCrypt hash
     */
    public static String generateHash(String password) {
        return hashPassword(password);
    }
}

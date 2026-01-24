package com.hotel.server;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility class for hashing and verifying passwords using BCrypt.
 */
public class PasswordHasher {
    private static final int BCRYPT_COST = 12;

    /**
     * Hashes a plain text password using BCrypt.
     *
     * @param plainPassword the plain text password to hash
     * @return the hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    /**
     * Verifies a plain text password against a hashed password.
     *
     * @param plainPassword  the plain text password
     * @param hashedPassword the hashed password to check against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}

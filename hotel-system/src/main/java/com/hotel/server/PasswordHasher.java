package com.hotel.server;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {
    private static final int BCRYPT_COST = 12;

    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}

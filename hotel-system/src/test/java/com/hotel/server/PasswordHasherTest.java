package com.hotel.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void testHashPassword() {
        String password = "mySecretPassword";
        String hashedPassword = PasswordHasher.hashPassword(password);

        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$")); // BCrypt prefix
    }

    @Test
    void testVerifyPassword() {
        String password = "password123";
        String hashedPassword = PasswordHasher.hashPassword(password);

        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword));
        assertFalse(PasswordHasher.verifyPassword("wrongPassword", hashedPassword));
    }
}

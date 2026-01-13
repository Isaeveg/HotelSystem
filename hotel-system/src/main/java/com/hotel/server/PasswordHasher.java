package com.hotel.server;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Утилитный класс для безопасного хеширования и проверки паролей с
 * использованием BCrypt
 */
public class PasswordHasher {
    private static final int BCRYPT_COST = 12; // Сложность хеширования (рекомендуется 12)

    /**
     * Хеширует пароль с использованием BCrypt
     * 
     * @param plainPassword Пароль в открытом виде
     * @return Хешированный пароль
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    /**
     * Проверяет, соответствует ли введенный пароль хешированному
     * 
     * @param plainPassword  Пароль в открытом виде
     * @param hashedPassword Хешированный пароль из базы данных
     * @return true если пароли совпадают, false в противном случае
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}

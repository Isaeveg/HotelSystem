package com.hotel.server;

import com.hotel.common.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Утилита для сброса пароля администратора
 */
public class ResetAdminPassword {

    public static void main(String[] args) {
        // Используем email как логин, чтобы соответствовать новой структуре БД
        String email = "admin@hotel.com";
        String password = "admin";
        String role = "ADMIN";

        // Хешируем пароль
        String hashedPassword = PasswordHasher.hashPassword(password);

        System.out.println("=== Сброс пароля администратора ===");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println("Hashed Password: " + hashedPassword);
        System.out.println();

        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword())) {

            // ! Ищем по колонке email, а не username
            String checkSql = "SELECT id FROM users WHERE email = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Пользователь существует - обновляем пароль
                        // ! Обновляем по колонке email
                        String updateSql = "UPDATE users SET password = ? WHERE email = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, hashedPassword);
                            updateStmt.setString(2, email);
                            int updated = updateStmt.executeUpdate();
                            System.out.println("✅ Пароль пользователя '" + email + "' успешно обновлён!");
                        }
                    } else {
                        // Пользователь не существует - создаём
                        // ! Вставляем в колонку email
                        String insertSql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?::user_role)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, email);
                            insertStmt.setString(2, hashedPassword);
                            insertStmt.setString(3, role);
                            int inserted = insertStmt.executeUpdate();
                            System.out.println("✅ Пользователь '" + email + "' успешно создан!");
                        }
                    }
                }
            }

            // Проверяем, что пароль работает
            System.out.println();
            System.out.println("=== Проверка входа ===");
            User user = DatabaseHandler.loginUser(email, password);
            if (user != null) {
                System.out.println("✅ Успешный вход с новым паролем!");
                System.out.println("   User ID: " + user.getId());
                System.out.println("   Email: " + user.getEmail()); // Используем getEmail()
                System.out.println("   Role: " + user.getRole());
            } else {
                System.out.println("❌ ОШИБКА: Не удалось войти с новым паролем!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при работе с базой данных:");
            e.printStackTrace();
        }
    }
}
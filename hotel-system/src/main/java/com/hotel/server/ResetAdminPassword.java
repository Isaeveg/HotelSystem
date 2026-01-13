package com.hotel.server;

import com.hotel.common.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Утилита для сброса пароля администратора
 * Запускается один раз для создания/обновления пароля admin
 */
public class ResetAdminPassword {

    public static void main(String[] args) {
        String username = "admin";
        String password = "admin";
        String role = "admin";

        // Хешируем пароль
        String hashedPassword = PasswordHasher.hashPassword(password);

        System.out.println("=== Сброс пароля администратора ===");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Hashed Password: " + hashedPassword);
        System.out.println();

        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword())) {

            // Проверяем, существует ли пользователь
            String checkSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Пользователь существует - обновляем пароль
                        String updateSql = "UPDATE users SET password = ? WHERE username = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, hashedPassword);
                            updateStmt.setString(2, username);
                            int updated = updateStmt.executeUpdate();
                            System.out.println("✅ Пароль пользователя '" + username + "' успешно обновлён!");
                            System.out.println("   Обновлено строк: " + updated);
                        }
                    } else {
                        // Пользователь не существует - создаём
                        String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, username);
                            insertStmt.setString(2, hashedPassword);
                            insertStmt.setString(3, role);
                            int inserted = insertStmt.executeUpdate();
                            System.out.println("✅ Пользователь '" + username + "' успешно создан!");
                            System.out.println("   Добавлено строк: " + inserted);
                        }
                    }
                }
            }

            // Проверяем, что пароль работает
            System.out.println();
            System.out.println("=== Проверка пароля ===");
            User user = DatabaseHandler.loginUser(username, password);
            if (user != null) {
                System.out.println("✅ Успешный вход с новым паролем!");
                System.out.println("   User ID: " + user.getId());
                System.out.println("   Username: " + user.getUsername());
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

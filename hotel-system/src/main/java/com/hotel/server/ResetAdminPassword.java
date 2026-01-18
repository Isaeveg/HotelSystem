package com.hotel.server;

import com.hotel.common.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResetAdminPassword {

    public static void main(String[] args) {
        String email = "admin@hotel.com";
        String password = "admin";
        String role = "ADMIN";

        String hashedPassword = PasswordHasher.hashPassword(password);

        System.out.println("=== Reset hasła administratora ===");
        System.out.println("Email: " + email);
        System.out.println("Hasło: " + password);
        System.out.println("Hasło (hash): " + hashedPassword);
        System.out.println();

        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword())) {

            String checkSql = "SELECT id FROM users WHERE email = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String updateSql = "UPDATE users SET password = ? WHERE email = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, hashedPassword);
                            updateStmt.setString(2, email);
                            System.out.println("✅ Hasło użytkownika '" + email + "' zaktualizowane pomyślnie!");
                        }
                    } else {
                        String insertSql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?::user_role)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, email);
                            insertStmt.setString(2, hashedPassword);
                            insertStmt.setString(3, role);
                            System.out.println("✅ Użytkownik '" + email + "' został pomyślnie utworzony!");
                        }
                    }
                }
            }

            System.out.println();
            System.out.println("=== Weryfikacja logowania ===");
            User user = DatabaseHandler.loginUser(email, password);
            if (user != null) {
                System.out.println("✅ Logowanie powiodło się z nowym hasłem!");
                System.out.println("   User ID: " + user.getId());
                System.out.println("   Email: " + user.getEmail());
                System.out.println("   Rola: " + user.getRole());
            } else {
                System.out.println("❌ BŁĄD: Nie udało się zalogować z nowym hasłem!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Błąd podczas operacji na bazie danych:");
            e.printStackTrace();
        }
    }
}
package com.hotel.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseHandler {

    private static final Properties properties = new Properties();
    private static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    static {
        try (InputStream input = DatabaseHandler.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.error("Błąd: Nie można znaleźć pliku database.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getDbConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String pass = properties.getProperty("db.password");

        return DriverManager.getConnection(url, user, pass);
    }

    public static void main(String[] args) {
        logger.info("Próba połączenia z bazą danych...");

        try (Connection connection = getDbConnection()) {
            if (connection != null) {
                logger.info("SUKCES! Połączenie z bazą danych zostało nawiązane..");
                logger.info("Schema: " + connection.getSchema());
            }
        } catch (SQLException e) {
            logger.error("BŁĄD POŁĄCZENIA!");
            logger.info("Szczegóły błędu: " + e.getMessage());
        }
    }

    public static com.hotel.common.User loginUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection dbConnection = getDbConnection();
             java.sql.PreparedStatement statement = dbConnection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (java.sql.ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    com.hotel.common.User user = new com.hotel.common.User();
                    user.setId(resultSet.getInt("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setRole(resultSet.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<com.hotel.common.Room> getAllRooms() {
        java.util.List<com.hotel.common.Room> rooms = new java.util.ArrayList<>();
        String query = "SELECT * FROM rooms";

        try (Connection dbConnection = getDbConnection();
             java.sql.PreparedStatement statement = dbConnection.prepareStatement(query);
             java.sql.ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                com.hotel.common.Room room = new com.hotel.common.Room(
                        resultSet.getInt("id"),
                        resultSet.getString("room_number"),
                        resultSet.getString("type"),
                        resultSet.getDouble("price_per_night"),
                        resultSet.getString("status")
                );
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public static boolean createBooking(int userId, int roomId, java.time.LocalDate from, java.time.LocalDate to) {
        String findClientSql = "SELECT id FROM clients WHERE user_id = ?";
        String insertBookingSql = "INSERT INTO bookings (client_id, room_id, check_in_date, check_out_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getDbConnection()) {
            int clientId = -1;

            try (java.sql.PreparedStatement ps = conn.prepareStatement(findClientSql)) {
                ps.setInt(1, userId);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    clientId = rs.getInt("id");
                } else {
                    logger.error("Błąd: Ten użytkownik nie ma profilu klienta");
                    return false;
                }
            }

            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertBookingSql)) {
                ps.setInt(1, clientId);
                ps.setInt(2, roomId);
                ps.setDate(3, java.sql.Date.valueOf(from));
                ps.setDate(4, java.sql.Date.valueOf(to));
                ps.setDouble(5, 500.0);
                ps.setObject(6, "CONFIRMED", java.sql.Types.OTHER);

                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addRoom(com.hotel.common.Room room) {
        String sql = "INSERT INTO rooms (room_number, type, price_per_night, floor, status, capacity) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, room.getNumber());
            ps.setString(2, room.getType());
            ps.setDouble(3, room.getPrice());
            ps.setInt(4, 1);
            ps.setObject(5, "FREE", java.sql.Types.OTHER);
            ps.setInt(6, 2);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = getDbConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerUser(com.hotel.common.User user) {
        String checkSql = "SELECT id FROM users WHERE username = ?";

        String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?::user_role) RETURNING id";
        String insertClientSql = "INSERT INTO clients (user_id, first_name, last_name, email) VALUES (?, ?, ?, ?)";

        try (java.sql.Connection conn = getDbConnection()) {
            conn.setAutoCommit(false);

            try {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, user.getUsername());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            logger.info("Login zajęty: " + user.getUsername());
                            conn.rollback();
                            return false;
                        }
                    }
                }

                int newUserId = -1;
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPassword());
                    ps.setString(3, "CLIENT");

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            newUserId = rs.getInt(1);
                        }
                    }
                }

                if (newUserId == -1) {
                    conn.rollback();
                    return false;
                }

                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertClientSql)) {
                    ps.setInt(1, newUserId);
                    ps.setString(2, "Gość");
                    ps.setString(3, "Nowy");
                    ps.setString(4, user.getUsername() + "@mail.com");
                    ps.executeUpdate();
                }

                conn.commit();
                logger.info("Użytkownik i profil klienta zostały utworzone! ID: " + newUserId);
                return true;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                logger.error("Błąd SQL podczas rejestracji: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
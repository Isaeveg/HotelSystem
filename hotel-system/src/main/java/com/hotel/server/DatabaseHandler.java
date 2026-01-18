package com.hotel.server;

import com.hotel.common.Room;
import com.hotel.common.User;
import com.hotel.common.Client;
import com.hotel.common.Hotel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword());
    }

    public static User loginUser(String email, String password) {
        String sql = "SELECT id, email, password, role FROM users WHERE email = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        logger.info("Pomyślna autentykacja użytkownika: {}", email);
                        return new User(
                                rs.getInt("id"),
                                rs.getString("email"),
                                rs.getString("role"));
                    } else {
                        logger.warn("Nieprawidłowe hasło dla użytkownika: {}", email);
                    }
                } else {
                    logger.warn("Użytkownik nie znaleziony: {}", email);
                }
            }
        } catch (SQLException e) {
            logger.error("Błąd DB podczas autentykacji użytkownika {}: {}", email, e.getMessage(), e);
        }
        return null;
    }

    public static boolean registerUser(String firstName, String lastName, String email, String phone, String password) {
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, ?::user_role) RETURNING id";
        String sqlClient = "INSERT INTO clients (user_id, first_name, last_name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                String hashedPassword = PasswordHasher.hashPassword(password);
                psUser.setString(1, email);
                psUser.setString(2, hashedPassword);
                psUser.setString(3, "CLIENT");

                int userId = -1;
                try (ResultSet rs = psUser.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    }
                }

                if (userId == -1) {
                    throw new SQLException("Nie udało się pobrać ID nowego użytkownika.");
                }

                try (PreparedStatement psClient = conn.prepareStatement(sqlClient)) {
                    psClient.setInt(1, userId);
                    psClient.setString(2, firstName);
                    psClient.setString(3, lastName);
                    psClient.setString(4, phone);

                    psClient.executeUpdate();
                }

                conn.commit();
                logger.info("Rejestracja zakończona sukcesem: {} (id={})", email, userId);
                return true;

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Wycofanie transakcji. Błąd rejestracji {}: {}", email, e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Błąd połączenia z DB: {}", e.getMessage());
            return false;
        }
    }

    public static List<Hotel> getHotels() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT id, name, city FROM hotels";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                hotels.add(new Hotel(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("city")));
            }
        } catch (SQLException e) {
            logger.error("Błąd pobierania listy hoteli: {}", e.getMessage());
        }
        return hotels;
    }

    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.hotel_id, r.room_number, r.type, r.price_per_night, r.status, r.description, h.name as hotel_name, h.city "
                +
                "FROM rooms r JOIN hotels h ON r.hotel_id = h.id";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String fullHotelName = rs.getString("city") + " - " + rs.getString("hotel_name");
                rooms.add(new Room(
                        rs.getInt("id"),
                        rs.getInt("hotel_id"),
                        rs.getString("room_number"),
                        rs.getString("type"),
                        rs.getString("price_per_night"),
                        rs.getString("status"),
                        rs.getString("description"),
                        fullHotelName));
            }
        } catch (SQLException e) {
            logger.error("Błąd DB podczas pobierania listy pokoi: {}", e.getMessage(), e);
        }
        return rooms;
    }

    public static boolean addRoom(int hotelId, String number, String type, String price, String description) {
        String sql = "INSERT INTO rooms (hotel_id, room_number, type, price_per_night, description, floor) VALUES (?, ?, ?, ?, ?, ?)";

        int floor = 1;
        try {
            if (number.length() > 0 && Character.isDigit(number.charAt(0))) {
                floor = Character.getNumericValue(number.charAt(0));
            }
        } catch (Exception e) {
        }

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            pstmt.setString(2, number);
            pstmt.setString(3, type);
            pstmt.setBigDecimal(4, new java.math.BigDecimal(price));
            pstmt.setString(5, description);
            pstmt.setInt(6, floor);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Błąd SQL podczas dodawania pokoju: {}", e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            logger.error("Błąd formatu ceny: {}", price);
            return false;
        }
    }

    public static boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Błąd usuwania pokoju {}: {}", roomId, e.getMessage());
            return false;
        }
    }

    public static boolean updateRoom(int id, String number, String type, String price, String description) {
        String sql = "UPDATE rooms SET room_number=?, type=?, price_per_night=?, description=? WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, number);
            pstmt.setString(2, type);
            pstmt.setBigDecimal(3, new java.math.BigDecimal(price));
            pstmt.setString(4, description);
            pstmt.setInt(5, id);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Błąd aktualizacji pokoju {}: {}", id, e.getMessage());
            return false;
        }
    }

    public static List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT c.id, c.first_name, c.last_name, c.phone, u.email " +
                "FROM clients c " +
                "JOIN users u ON c.user_id = u.id";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone")));
            }
        } catch (SQLException e) {
            logger.error("Błąd pobierania listy klientów: {}", e.getMessage());
        }
        return clients;
    }

    public static boolean addClient(String firstName, String lastName, String email, String password, String phone) {
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, ?::user_role) RETURNING id";
        String sqlClient = "INSERT INTO clients (user_id, first_name, last_name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String hashedPassword = PasswordHasher.hashPassword(password);
                int userId = -1;

                try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                    psUser.setString(1, email);
                    psUser.setString(2, hashedPassword);
                    psUser.setString(3, "CLIENT");

                    try (ResultSet rs = psUser.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id");
                        }
                    }
                }

                if (userId == -1) {
                    throw new SQLException("Nie udało się utworzyć użytkownika (brak ID).");
                }

                try (PreparedStatement psClient = conn.prepareStatement(sqlClient)) {
                    psClient.setInt(1, userId);
                    psClient.setString(2, firstName);
                    psClient.setString(3, lastName);
                    psClient.setString(4, phone);

                    psClient.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Błąd dodawania klienta (transakcja wycofana): {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Błąd połączenia z DB: {}", e.getMessage());
            return false;
        }
    }

    public static boolean deleteClient(int clientId) {
        String getUserIdSql = "SELECT user_id FROM clients WHERE id = ?";
        String deleteUserSql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = getConnection()) {
            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(getUserIdSql)) {
                ps.setInt(1, clientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        userId = rs.getInt("user_id");
                }
            }

            if (userId != -1) {
                try (PreparedStatement ps = conn.prepareStatement(deleteUserSql)) {
                    ps.setInt(1, userId);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Błąd usuwania klienta {}: {}", clientId, e.getMessage());
        }
        return false;
    }

    public static boolean updateClient(int clientId, String firstName, String lastName, String email, String phone) {
        String getUserIdSql = "SELECT user_id FROM clients WHERE id = ?";
        String updateClientSql = "UPDATE clients SET first_name=?, last_name=?, phone=? WHERE id=?";
        String updateUserSql = "UPDATE users SET email=? WHERE id=?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = -1;
                try (PreparedStatement psGetId = conn.prepareStatement(getUserIdSql)) {
                    psGetId.setInt(1, clientId);
                    try (ResultSet rs = psGetId.executeQuery()) {
                        if (rs.next())
                            userId = rs.getInt("user_id");
                    }
                }

                if (userId == -1) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement psClient = conn.prepareStatement(updateClientSql)) {
                    psClient.setString(1, firstName);
                    psClient.setString(2, lastName);
                    psClient.setString(3, phone);
                    psClient.setInt(4, clientId);
                    psClient.executeUpdate();
                }

                try (PreparedStatement psUser = conn.prepareStatement(updateUserSql)) {
                    psUser.setString(1, email);
                    psUser.setInt(2, userId);
                    psUser.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Błąd podczas aktualizacji: {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Błąd połączenia: {}", e.getMessage());
            return false;
        }
    }
}

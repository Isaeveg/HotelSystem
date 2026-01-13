package com.hotel.server;

import com.hotel.common.Room;
import com.hotel.common.User;
import com.hotel.common.Hotel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик взаимодействия с базой данных
 */
public class DatabaseHandler {
    private static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword());
    }

    /**
     * Аутентификация пользователя с проверкой хешированного пароля
     * 
     * @param username Имя пользователя
     * @param password Пароль в открытом виде
     * @return Объект User если аутентификация успешна, null в противном случае
     */
    public static User loginUser(String username, String password) {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    // Проверяем пароль с использованием BCrypt
                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        logger.info("Успешная аутентификация пользователя: {}", username);
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("role"));
                    } else {
                        logger.warn("Неверный пароль для пользователя: {}", username);
                    }
                } else {
                    logger.warn("Пользователь не найден: {}", username);
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка БД при аутентификации пользователя {}: {}", username, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Регистрация нового пользователя с хешированием пароля
     * 
     * @param username Имя пользователя
     * @param password Пароль в открытом виде
     * @param role     Роль пользователя (admin/client)
     * @return true если регистрация успешна, false в противном случае
     */
    public static boolean registerUser(String username, String password, String fullName) {
        // SQL для таблицы users (добавляем RETURNING id, чтобы сразу получить ID нового
        // юзера)
        String sqlUser = "INSERT INTO users (username, password, role) VALUES (?, ?, ?::user_role) RETURNING id";
        // SQL для таблицы clients
        String sqlClient = "INSERT INTO clients (user_id, first_name, last_name, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            // Включаем ручное управление транзакцией
            conn.setAutoCommit(false);

            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                // 1. Вставляем в USERS
                String hashedPassword = PasswordHasher.hashPassword(password);
                psUser.setString(1, username);
                psUser.setString(2, hashedPassword);
                psUser.setString(3, "CLIENT"); // Роль по умолчанию

                int userId = -1;
                try (ResultSet rs = psUser.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    }
                }

                if (userId == -1) {
                    throw new SQLException("Не удалось получить ID пользователя.");
                }

                // 2. Вставляем в CLIENTS
                // Разбиваем строку "Ivan Ivanov" на имя и фамилию
                String[] parts = fullName.trim().split("\\s+", 2);
                String firstName = parts[0];
                String lastName = parts.length > 1 ? parts[1] : "-"; // Если фамилии нет, ставим прочерк

                try (PreparedStatement psClient = conn.prepareStatement(sqlClient)) {
                    psClient.setInt(1, userId);
                    psClient.setString(2, firstName);
                    psClient.setString(3, lastName);
                    psClient.setString(4, username); // Email используем тот же, что и логин

                    psClient.executeUpdate();
                }

                // Если всё прошло успешно — фиксируем изменения
                conn.commit();
                logger.info("Успешная полная регистрация: {} (id={})", username, userId);
                return true;

            } catch (SQLException e) {
                // Если ошибка — отменяем всё, что успели сделать
                conn.rollback();
                logger.error("Транзакция отменена. Ошибка регистрации {}: {}", username, e.getMessage());
                return false;
            } finally {
                // Возвращаем авто-коммит обратно
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Ошибка подключения к БД: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Получить список отелей для выпадающего списка
     */
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
            logger.error("Ошибка получения отелей: {}", e.getMessage());
        }
        return hotels;
    }

    /**
     * Теперь вытаскиваем комнаты вместе с названием отеля
     */
    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        // JOIN, чтобы узнать город и название отеля
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
                        rs.getInt("hotel_id"), // ID отеля
                        rs.getString("room_number"),
                        rs.getString("type"),
                        rs.getString("price_per_night"),
                        rs.getString("status"),
                        rs.getString("description"),
                        fullHotelName // Название отеля
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка БД при получении списка комнат: {}", e.getMessage(), e);
        }
        return rooms;
    }

    /**
     * Добавление комнаты ТЕПЕРЬ С hotelId
     */
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

            pstmt.setInt(1, hotelId); // <-- ВОТ ОНО
            pstmt.setString(2, number);
            pstmt.setString(3, type);
            pstmt.setBigDecimal(4, new java.math.BigDecimal(price));
            pstmt.setString(5, description);
            pstmt.setInt(6, floor);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка SQL при добавлении комнаты: {}", e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            logger.error("Ошибка формата цены: {}", price);
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
            logger.error("Ошибка удаления комнаты {}: {}", roomId, e.getMessage());
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
            logger.error("Ошибка обновления комнаты {}: {}", id, e.getMessage());
            return false;
        }
    }
}

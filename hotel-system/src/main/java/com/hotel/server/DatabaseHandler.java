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
    public static User loginUser(String email, String password) {
        // Запрашиваем email вместо username
        String sql = "SELECT id, email, password, role FROM users WHERE email = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    // Проверяем пароль
                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        logger.info("Успешная аутентификация пользователя: {}", email);

                        // ! ИСПРАВЛЕНИЕ ТУТ:
                        // Раньше было rs.getString("username"), а теперь берем "email"
                        return new User(
                                rs.getInt("id"),
                                rs.getString("email"),
                                rs.getString("role"));
                    } else {
                        logger.warn("Неверный пароль для пользователя: {}", email);
                    }
                } else {
                    logger.warn("Пользователь не найден: {}", email);
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка БД при аутентификации пользователя {}: {}", email, e.getMessage(), e);
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
    public static boolean registerUser(String firstName, String lastName, String email, String phone, String password) {
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, ?::user_role) RETURNING id";
        String sqlClient = "INSERT INTO clients (user_id, first_name, last_name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Транзакция

            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                // 1. Создаем пользователя
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
                    throw new SQLException("Не удалось получить ID нового пользователя.");
                }

                // 2. Создаем запись клиента
                try (PreparedStatement psClient = conn.prepareStatement(sqlClient)) {
                    psClient.setInt(1, userId);
                    psClient.setString(2, firstName);
                    psClient.setString(3, lastName);
                    psClient.setString(4, phone); // Теперь пишем телефон

                    psClient.executeUpdate();
                }

                conn.commit();
                logger.info("Регистрация успешна: {} (id={})", email, userId);
                return true;

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Откат транзакции. Ошибка регистрации {}: {}", email, e.getMessage());
                return false;
            } finally {
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

    /**
     * Получение списка всех клиентов
     */
    public static List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        // 1. Добавляем c.phone в SELECT
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
                        rs.getString("phone") // <--- 2. Передаем телефон в конструктор
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения списка клиентов: {}", e.getMessage());
        }
        return clients;
    }

    public static boolean addClient(String firstName, String lastName, String email, String password, String phone) {
        // Крок 1: Вставляємо в USERS (тут email ПОТРІБЕН)
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, ?::user_role) RETURNING id";

        // Крок 2: Вставляємо в CLIENTS (тут email НЕМАЄ, згідно з твоїм SQL скриптом)
        // Було: VALUES (?, ?, ?, ?, ?) - 5 параметрів
        // Стало: VALUES (?, ?, ?, ?) - 4 параметри (user_id, first_name, last_name,
        // phone)
        String sqlClient = "INSERT INTO clients (user_id, first_name, last_name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Починаємо транзакцію
            try {
                // --- 1. Створюємо User ---
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
                    throw new SQLException("Не вдалося створити користувача (ID не повернувся).");
                }

                // --- 2. Створюємо Client ---
                try (PreparedStatement psClient = conn.prepareStatement(sqlClient)) {
                    psClient.setInt(1, userId); // 1. user_id
                    psClient.setString(2, firstName); // 2. first_name
                    psClient.setString(3, lastName); // 3. last_name
                    // psClient.setString(4, email); <-- ВИДАЛЯЄМО ЦЕ!
                    psClient.setString(4, phone); // 4. phone (тепер це 4-й параметр)

                    psClient.executeUpdate();
                }

                conn.commit(); // Фіксуємо зміни
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Відкочуємо все, якщо була помилка
                logger.error("Помилка додавання клієнта (транзакцію відкочено): {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Помилка підключення до БД: {}", e.getMessage());
            return false;
        }
    }

    public static boolean deleteClient(int clientId) {
        // Сначала узнаем user_id, чтобы удалить пользователя (каскадно удалится и
        // клиент)
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
            logger.error("Ошибка удаления клиента {}: {}", clientId, e.getMessage());
        }
        return false;
    }

    public static boolean updateClient(int clientId, String firstName, String lastName, String email, String phone) {
        String getUserIdSql = "SELECT user_id FROM clients WHERE id = ?";
        // Исправленный SQL (email обновляем в таблице users, здесь он не нужен, если
        // его нет в clients)
        String updateClientSql = "UPDATE clients SET first_name=?, last_name=?, phone=? WHERE id=?";
        String updateUserSql = "UPDATE users SET email=? WHERE id=?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Узнаем user_id
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

                // 2. Обновляем clients (ИСПРАВЛЕНО СООТВЕТСТВИЕ ПАРАМЕТРОВ)
                try (PreparedStatement psClient = conn.prepareStatement(updateClientSql)) {
                    psClient.setString(1, firstName);
                    psClient.setString(2, lastName);
                    psClient.setString(3, phone); // Третий параметр - телефон
                    psClient.setInt(4, clientId); // Четвертый - ID для WHERE
                    psClient.executeUpdate();
                }

                // 3. Обновляем email в users
                try (PreparedStatement psUser = conn.prepareStatement(updateUserSql)) {
                    psUser.setString(1, email);
                    psUser.setInt(2, userId);
                    psUser.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Ошибка при обновлении: {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Ошибка подключения: {}", e.getMessage());
            return false;
        }
    }
}

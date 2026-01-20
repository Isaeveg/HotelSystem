package com.hotel.server;

import com.hotel.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseHandler {
    private static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    // --- SQL CONSTANTS ---
    private static final String SQL_LOGIN = "SELECT id, email, password, role FROM users WHERE email = ?";
    private static final String SQL_REGISTER_CALL = "CALL register_client(?, ?, ?, ?, ?)";
    private static final String SQL_GET_HOTELS = "SELECT id, name, city FROM hotels";
    private static final String SQL_GET_ROOMS = "SELECT r.id, r.hotel_id, r.room_number, r.type, r.price_per_night, r.status, r.description, h.name as hotel_name, h.city "
            +
            "FROM rooms r JOIN hotels h ON r.hotel_id = h.id";
    private static final String SQL_ADD_ROOM = "INSERT INTO rooms (hotel_id, room_number, type, price_per_night, description, floor) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_ROOM = "DELETE FROM rooms WHERE id = ?";
    private static final String SQL_UPDATE_ROOM = "UPDATE rooms SET room_number=?, type=?, price_per_night=?, description=? WHERE id=?";

    private static final String SQL_GET_CLIENTS = "SELECT c.id, c.first_name, c.last_name, c.phone, u.email FROM clients c JOIN users u ON c.user_id = u.id";

    private static final String SQL_GET_BOOKINGS = "SELECT b.id, b.client_id, b.room_id, b.check_in_date, b.check_out_date, b.total_price, b.status, u.email, r.room_number, h.name as hotel_name, "
            +
            "(SELECT array_agg(amenity_id) FROM booking_amenities WHERE booking_id = b.id) as amenity_ids " +
            "FROM bookings b " +
            "JOIN clients c ON b.client_id = c.id JOIN users u ON c.user_id = u.id " +
            "JOIN rooms r ON b.room_id = r.id JOIN hotels h ON r.hotel_id = h.id " +
            "ORDER BY b.id DESC";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword());
    }

    // USER

    public static User loginUser(String email, String password) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_LOGIN)) {

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
        try (Connection conn = getConnection();
                CallableStatement cstmt = conn.prepareCall(SQL_REGISTER_CALL)) {

            String hashedPassword = PasswordHasher.hashPassword(password);

            cstmt.setString(1, email);
            cstmt.setString(2, hashedPassword);
            cstmt.setString(3, firstName);
            cstmt.setString(4, lastName);
            cstmt.setString(5, phone);

            cstmt.execute();
            logger.info("Rejestracja zakończona sukcesem (via procedure): {}", email);
            return true;

        } catch (SQLException e) {
            logger.error("Błąd procedury rejestracji {}: {}", email, e.getMessage());
            return false;
        }
    }

    // HOTELS AND ROOMS

    public static List<Hotel> getHotels() {
        List<Hotel> hotels = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_HOTELS)) {

            while (rs.next()) {
                hotels.add(new Hotel(rs.getInt("id"), rs.getString("name"), rs.getString("city")));
            }
        } catch (SQLException e) {
            logger.error("Błąd pobierania listy hoteli: {}", e.getMessage());
        }
        return hotels;
    }

    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_ROOMS)) {

            while (rs.next()) {
                rooms.add(mapToRoom(rs));
            }
        } catch (SQLException e) {
            logger.error("Błąd DB podczas pobierania listy pokoi: {}", e.getMessage(), e);
        }
        return rooms;
    }

    public static boolean addRoom(int hotelId, String number, String type, String price, String description) {
        int floor = calculateFloor(number);

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_ADD_ROOM)) {

            pstmt.setInt(1, hotelId);
            pstmt.setString(2, number);
            pstmt.setString(3, type);
            pstmt.setBigDecimal(4, new BigDecimal(price));
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
        return executeUpdate(SQL_DELETE_ROOM, roomId);
    }

    public static boolean updateRoom(int id, String number, String type, String price, String description) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_ROOM)) {

            pstmt.setString(1, number);
            pstmt.setString(2, type);
            pstmt.setBigDecimal(3, new BigDecimal(price));
            pstmt.setString(4, description);
            pstmt.setInt(5, id);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Błąd aktualizacji pokoju {}: {}", id, e.getMessage());
            return false;
        }
    }

    // CLIENTS

    public static List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_CLIENTS)) {

            while (rs.next()) {
                clients.add(mapToClient(rs));
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
                int userId = insertUser(conn, sqlUser, email, hashedPassword);

                if (userId == -1)
                    throw new SQLException("Brak ID użytkownika.");

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
                logger.error("Błąd dodawania klienta: {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Błąd połączenia: {}", e.getMessage());
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
                try (PreparedStatement ps = conn.prepareStatement(getUserIdSql)) {
                    ps.setInt(1, clientId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            userId = rs.getInt("user_id");
                    }
                }

                if (userId == -1) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement(updateClientSql)) {
                    ps.setString(1, firstName);
                    ps.setString(2, lastName);
                    ps.setString(3, phone);
                    ps.setInt(4, clientId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateUserSql)) {
                    ps.setString(1, email);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Błąd połączenia: {}", e.getMessage());
            return false;
        }
    }

    // BOOKINGS

    public static List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_BOOKINGS)) {

            while (rs.next()) {
                list.add(mapToBooking(rs));
            }
        } catch (SQLException e) {
            logger.error("Błąd pobierania rezerwacji: {}", e.getMessage());
        }
        return list;
    }

    public static boolean addBooking(int clientId, int roomId, String inDate, String outDate, String price,
            String status, List<Integer> amenityIds) {
        String sqlBooking = "INSERT INTO bookings (client_id, room_id, check_in_date, check_out_date, total_price, status) "
                +
                "VALUES (?, ?, ?::date, ?::date, ?, ?::booking_status) RETURNING id";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookingId = -1;
                try (PreparedStatement ps = conn.prepareStatement(sqlBooking)) {
                    ps.setInt(1, clientId);
                    ps.setInt(2, roomId);
                    ps.setString(3, inDate);
                    ps.setString(4, outDate);
                    ps.setBigDecimal(5, new BigDecimal(price));
                    ps.setString(6, status);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            bookingId = rs.getInt("id");
                    }
                }

                if (bookingId != -1) {
                    insertBookingAmenities(conn, bookingId, amenityIds);
                }

                conn.commit();
                logger.info("Rezerwacja z usługami dodana, ID: {}", bookingId);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Błąd transakcji rezerwacji: {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean deleteBooking(int id) {
        return executeUpdate("DELETE FROM bookings WHERE id = ?", id);
    }

    public static boolean updateBooking(int id, int clientId, int roomId, String inDate, String outDate, String price,
            String status, List<Integer> amenityIds) {
        String sqlUpdate = "UPDATE bookings SET client_id=?, room_id=?, check_in_date=?::date, check_out_date=?::date, "
                +
                "total_price=?, status=?::booking_status WHERE id=?";
        String sqlDeleteAm = "DELETE FROM booking_amenities WHERE booking_id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, clientId);
                    ps.setInt(2, roomId);
                    ps.setString(3, inDate);
                    ps.setString(4, outDate);
                    ps.setBigDecimal(5, new BigDecimal(price));
                    ps.setString(6, status);
                    ps.setInt(7, id);
                    ps.executeUpdate();
                }

                try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteAm)) {
                    psDel.setInt(1, id);
                    psDel.executeUpdate();
                }
                insertBookingAmenities(conn, id, amenityIds);

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Błąd aktualizacji rezerwacji {}: {}", id, e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // DASHBOARD

    public static List<Amenity> getAllAmenities() {
        List<Amenity> list = new ArrayList<>();
        String sql = "SELECT id, name, price FROM amenities";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Amenity(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price").doubleValue()));
            }
        } catch (SQLException e) {
            logger.error("Błąd pobierania usług: {}", e.getMessage());
        }
        return list;
    }

    public static DashboardData getDashboardStats() {
        int todayCount = 0;
        double monthIncome = 0.0;
        List<DashboardData.ActivityEntry> activities = new ArrayList<>();

        String sqlCount = "SELECT COUNT(*) FROM bookings WHERE created_at::date = CURRENT_DATE";
        String sqlIncomeFunc = "SELECT get_monthly_income(?, ?)";
        String sqlActivity = "SELECT b.created_at, u.email, r.room_number, b.status " +
                "FROM bookings b JOIN clients c ON b.client_id = c.id " +
                "JOIN users u ON c.user_id = u.id JOIN rooms r ON b.room_id = r.id " +
                "ORDER BY b.created_at DESC LIMIT 15";

        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlCount)) {
                if (rs.next())
                    todayCount = rs.getInt(1);
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlIncomeFunc)) {
                LocalDate now = LocalDate.now();
                ps.setInt(1, now.getMonthValue());
                ps.setInt(2, now.getYear());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        monthIncome = rs.getDouble(1);
                }
            }

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlActivity)) {
                while (rs.next()) {
                    activities.add(mapToActivityEntry(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Błąd pobierania dashboardu: {}", e.getMessage());
        }
        return new DashboardData(todayCount, monthIncome, activities);
    }

    // HELPER

    private static boolean executeUpdate(String sql, int id) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Błąd wykonania update/delete: {}", e.getMessage());
            return false;
        }
    }

    private static int insertUser(Connection conn, String sql, String email, String passwordHash) throws SQLException {
        try (PreparedStatement psUser = conn.prepareStatement(sql)) {
            psUser.setString(1, email);
            psUser.setString(2, passwordHash);
            psUser.setString(3, "CLIENT");
            try (ResultSet rs = psUser.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }
        return -1;
    }

    private static void insertBookingAmenities(Connection conn, int bookingId, List<Integer> amenityIds)
            throws SQLException {
        if (amenityIds == null || amenityIds.isEmpty())
            return;

        String sql = "INSERT INTO booking_amenities (booking_id, amenity_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer amId : amenityIds) {
                ps.setInt(1, bookingId);
                ps.setInt(2, amId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static int calculateFloor(String number) {
        try {
            if (number != null && !number.isEmpty() && Character.isDigit(number.charAt(0))) {
                return Character.getNumericValue(number.charAt(0));
            }
        } catch (Exception ignored) {
        }
        return 1;
    }

    // --- MAPPERS ---

    private static Room mapToRoom(ResultSet rs) throws SQLException {
        String fullHotelName = rs.getString("city") + " - " + rs.getString("hotel_name");
        return new Room(
                rs.getInt("id"),
                rs.getInt("hotel_id"),
                rs.getString("room_number"),
                rs.getString("type"),
                rs.getString("price_per_night"),
                rs.getString("status"),
                rs.getString("description"),
                fullHotelName);
    }

    private static Client mapToClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"));
    }

    private static Booking mapToBooking(ResultSet rs) throws SQLException {
        String fullRoomName = rs.getString("hotel_name") + " [" + rs.getString("room_number") + "]";
        List<Integer> amIds = new ArrayList<>();
        Array sqlArray = rs.getArray("amenity_ids");
        if (sqlArray != null) {
            Integer[] arr = (Integer[]) sqlArray.getArray();
            if (arr != null)
                Collections.addAll(amIds, arr);
        }
        return new Booking(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getInt("room_id"),
                rs.getString("email"),
                fullRoomName,
                rs.getString("check_in_date"),
                rs.getString("check_out_date"),
                rs.getString("total_price"),
                rs.getString("status"),
                amIds);
    }

    private static DashboardData.ActivityEntry mapToActivityEntry(ResultSet rs) throws SQLException {
        String time = rs.getTimestamp("created_at").toString();
        if (time.contains("."))
            time = time.substring(0, time.lastIndexOf(":"));

        String desc = "Rezerwacja: " + rs.getString("email") + " (Pokój " + rs.getString("room_number") + ")";
        String status = rs.getString("status");
        return new DashboardData.ActivityEntry(time, desc, status);
    }
}
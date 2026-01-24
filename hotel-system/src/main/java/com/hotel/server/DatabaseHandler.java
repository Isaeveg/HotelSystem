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

/**
 * Handles all database interactions for the application.
 * <p>
 * This class provides static methods to perform CRUD operations on users,
 * rooms, bookings,
 * clients, and other entities in the database.
 * </p>
 */
public class DatabaseHandler {
    private static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    // --- SQL CONSTANTS ---
    private static final String SQL_LOGIN = "SELECT u.id, u.email, u.password, u.role, c.id as client_id " +
            "FROM users u LEFT JOIN clients c ON u.id = c.user_id WHERE u.email = ?";
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

    private static final String SQL_ADD_FAVORITE = "INSERT INTO favorite_rooms (client_id, room_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    private static final String SQL_REMOVE_FAVORITE = "DELETE FROM favorite_rooms WHERE client_id = ? AND room_id = ?";
    private static final String SQL_GET_FAVORITES = "SELECT r.id, r.hotel_id, r.room_number, r.type, r.price_per_night, r.status, r.description, h.name as hotel_name, h.city "
            +
            "FROM favorite_rooms fr " +
            "JOIN rooms r ON fr.room_id = r.id " +
            "JOIN hotels h ON r.hotel_id = h.id " +
            "WHERE fr.client_id = ?";

    /**
     * Establishes a connection to the database.
     *
     * @return a {@link Connection} object
     * @throws SQLException if a database access error occurs
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword());
    }

    // USER

    /**
     * Authenticates a user with email and password.
     *
     * @param email    the user's email
     * @param password the raw password
     * @return a {@link User} object if authentication is successful, null otherwise
     */
    public static User loginUser(String email, String password) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_LOGIN)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        logger.info("Successful user authentication: {}", email);

                        int clientId = rs.getInt("client_id");

                        return new User(
                                rs.getInt("id"),
                                clientId,
                                rs.getString("email"),
                                rs.getString("role"));
                    } else {
                        logger.warn("Invalid password for user: {}", email);
                    }
                } else {
                    logger.warn("User not found: {}", email);
                }
            }
        } catch (SQLException e) {
            logger.error("DB login error {}: {}", email, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Registers a new user/client in the database.
     *
     * @param firstName the first name
     * @param lastName  the last name
     * @param email     the email address
     * @param phone     the phone number
     * @param password  the raw password
     * @return true if registration was successful, false otherwise
     */
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
            logger.info("Registration successful (via procedure): {}", email);
            return true;

        } catch (SQLException e) {
            logger.error("Registration procedure error {}: {}", email, e.getMessage());
            return false;
        }
    }

    // HOTELS AND ROOMS

    /**
     * Retrieves a list of all hotels.
     *
     * @return a list of {@link Hotel} objects
     */
    public static List<Hotel> getHotels() {
        List<Hotel> hotels = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_HOTELS)) {

            while (rs.next()) {
                hotels.add(new Hotel(rs.getInt("id"), rs.getString("name"), rs.getString("city")));
            }
        } catch (SQLException e) {
            logger.error("Error fetching hotel list: {}", e.getMessage());
        }
        return hotels;
    }

    /**
     * Retrieves all rooms from the database.
     *
     * @return a list of {@link Room} objects
     */
    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_ROOMS)) {

            while (rs.next()) {
                rooms.add(mapToRoom(rs));
            }
        } catch (SQLException e) {
            logger.error("DB error fetching room list: {}", e.getMessage(), e);
        }
        return rooms;
    }

    /**
     * Adds a new room to the database.
     *
     * @param hotelId     the hotel ID
     * @param number      the room number
     * @param type        the room type
     * @param price       the price per night
     * @param description the room description
     * @return true if added successfully, false otherwise
     */
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
            logger.error("SQL error adding room: {}", e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            logger.error("Price format error: {}", price);
            return false;
        }
    }

    /**
     * Deletes a room by its ID.
     *
     * @param roomId the room ID
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteRoom(int roomId) {
        return executeUpdate(SQL_DELETE_ROOM, roomId);
    }

    /**
     * Updates an existing room's details.
     *
     * @param id          the room ID
     * @param number      the new room number
     * @param type        the new type
     * @param price       the new price
     * @param description the new description
     * @return true if updated successfully, false otherwise
     */
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
            logger.error("Error updating room {}: {}", id, e.getMessage());
            return false;
        }
    }

    // CLIENTS

    /**
     * Retrieves details of a specific client.
     *
     * @param clientId the client ID
     * @return a {@link Client} object or null if not found
     */
    public static Client getClientDetails(int clientId) {
        Client client = null;
        String sql = "SELECT c.id, c.first_name, c.last_name, c.phone, u.email " +
                "FROM clients c JOIN users u ON c.user_id = u.id " +
                "WHERE c.id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    client = mapToClient(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching client details {}: {}", clientId, e.getMessage());
        }
        return client;
    }

    /**
     * Retrieves a list of all clients.
     *
     * @return a list of {@link Client} objects
     */
    public static List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_CLIENTS)) {

            while (rs.next()) {
                clients.add(mapToClient(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching client list: {}", e.getMessage());
        }
        return clients;
    }

    /**
     * Adds a new client (and user) to the database.
     *
     * @param firstName the first name
     * @param lastName  the last name
     * @param email     the email
     * @param password  the password
     * @param phone     the phone number
     * @return true if added successfully, false otherwise
     */
    public static boolean addClient(String firstName, String lastName, String email, String password, String phone) {
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, ?::user_role) RETURNING id";
        String sqlClient = "INSERT INTO clients (user_id, first_name, last_name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String hashedPassword = PasswordHasher.hashPassword(password);
                int userId = insertUser(conn, sqlUser, email, hashedPassword);

                if (userId == -1)
                    throw new SQLException("No user ID.");

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
                logger.error("Error adding client: {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Connection error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a client and their associated user account.
     *
     * @param clientId the client ID
     * @return true if deleted successfully, false otherwise
     */
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
            logger.error("Error deleting client {}: {}", clientId, e.getMessage());
        }
        return false;
    }

    /**
     * Updates client and user information.
     *
     * @param clientId  the client ID
     * @param firstName the new first name
     * @param lastName  the new last name
     * @param email     the new email
     * @param phone     the new phone number
     * @return true if updated successfully, false otherwise
     */
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
            logger.error("Connection error: {}", e.getMessage());
            return false;
        }
    }

    // BOOKINGS

    /**
     * Retrieves all bookings.
     *
     * @return a list of {@link Booking} objects
     */
    public static List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SQL_GET_BOOKINGS)) {

            while (rs.next()) {
                list.add(mapToBooking(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bookings: {}", e.getMessage());
        }
        return list;
    }

    /**
     * Adds a new booking.
     *
     * @param clientId   the client ID
     * @param roomId     the room ID
     * @param inDate     check-in date
     * @param outDate    check-out date
     * @param price      total price
     * @param status     booking status
     * @param amenityIds list of selected amenity IDs
     * @return true if added successfully, false otherwise
     */
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
                logger.info("Booking with amenities added, ID: {}", bookingId);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Booking transaction error: {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Deletes a booking by ID.
     *
     * @param id the booking ID
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteBooking(int id) {
        return executeUpdate("DELETE FROM bookings WHERE id = ?", id);
    }

    /**
     * Updates an existing booking.
     *
     * @param id         the booking ID
     * @param clientId   the client ID
     * @param roomId     the room ID
     * @param inDate     check-in date
     * @param outDate    check-out date
     * @param price      total price
     * @param status     status
     * @param amenityIds list of amenity IDs
     * @return true if updated successfully, false otherwise
     */
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
                logger.error("Error updating booking {}: {}", id, e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // DASHBOARD

    /**
     * Retrieves all amenities from the database.
     *
     * @return a list of {@link Amenity} objects
     */
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
            logger.error("Error fetching amenities: {}", e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves statistics for the dashboard.
     *
     * @return a {@link DashboardData} object containing stats
     */
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
            logger.error("Error fetching dashboard: {}", e.getMessage());
        }
        return new DashboardData(todayCount, monthIncome, activities);
    }

    // HELPER

    /**
     * Helper method to execute an update or delete SQL statement.
     *
     * @param sql the SQL statement
     * @param id  the integer parameter for the statement
     * @return true if at least one row was affected
     */
    private static boolean executeUpdate(String sql, int id) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error executing update/delete: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Inserts a user into the DB.
     *
     * @param conn         active connection
     * @param sql          SQL statement
     * @param email        user email
     * @param passwordHash hashed password
     * @return the generated user ID orn -1 on failure
     * @throws SQLException if SQL error occurs
     */
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

    /**
     * Inserts amenities for a booking.
     *
     * @param conn       active connection
     * @param bookingId  booking ID
     * @param amenityIds list of amenity IDs
     * @throws SQLException if SQL error occurs
     */
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

    /**
     * Calculates the floor based on the first digit of the room number.
     *
     * @param number the room number
     * @return the floor number
     */
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

        String desc = "Booking: " + rs.getString("email") + " (Room " + rs.getString("room_number") + ")";
        String status = rs.getString("status");
        return new DashboardData.ActivityEntry(time, desc, status);
    }

    /**
     * Adds a room to a client's favorites.
     *
     * @param clientId client ID
     * @param roomId   room ID
     * @return true if added, false otherwise
     */
    public static boolean addFavorite(int clientId, int roomId) {
        return executeUpdate(SQL_ADD_FAVORITE, clientId, roomId);
    }

    /**
     * Removes a room from a client's favorites.
     *
     * @param clientId client ID
     * @param roomId   room ID
     * @return true if removed, false otherwise
     */
    public static boolean removeFavorite(int clientId, int roomId) {
        return executeUpdate(SQL_REMOVE_FAVORITE, clientId, roomId);
    }

    /**
     * Retrieves a client's favorite rooms.
     *
     * @param clientId client ID
     * @return list of favorite rooms
     */
    public static List<Room> getFavorites(int clientId) {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SQL_GET_FAVORITES)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapToRoom(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching favorites: {}", e.getMessage());
        }
        return rooms;
    }

    /**
     * Helper to execute update with 2 params.
     *
     * @param sql SQL
     * @param p1  param 1
     * @param p2  param 2
     * @return true if success
     */
    private static boolean executeUpdate(String sql, int p1, int p2) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, p1);
            pstmt.setInt(2, p2);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error executeUpdate (2 params): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Searches for available rooms matching criteria.
     *
     * @param city     city name
     * @param dateFrom start date
     * @param dateTo   end date
     * @return list of available rooms
     */
    public static List<Room> searchFreeRooms(String city, LocalDate dateFrom, LocalDate dateTo) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.hotel_id, r.room_number, r.type, r.price_per_night, r.status, r.description, h.name as hotel_name, h.city "
                +
                "FROM rooms r " +
                "JOIN hotels h ON r.hotel_id = h.id " +
                "WHERE LOWER(h.city) LIKE LOWER(?) " +
                "AND r.id NOT IN (" +
                "    SELECT b.room_id FROM bookings b " +
                "    WHERE b.status != 'CANCELLED' " +
                "    AND NOT (b.check_out_date <= ? OR b.check_in_date >= ?)" +
                ")";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + city + "%");
            pstmt.setDate(2, java.sql.Date.valueOf(dateFrom));
            pstmt.setDate(3, java.sql.Date.valueOf(dateTo));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Room r = mapToRoom(rs);
                    r.setStatus("FREE");
                    rooms.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching rooms: {}", e.getMessage(), e);
        }
        return rooms;
    }

    /**
     * Checks bookings and updates room statuses.
     * <p>
     * Marks rooms as OCCUPIED if there is an active booking for today.
     * Marks rooms as FREE if they are no longer occupied.
     * </p>
     */
    public static void checkAndVerifyBookings() {
        String resetOccupiedSql = "UPDATE rooms SET status = 'FREE' WHERE status = 'OCCUPIED' AND id NOT IN (" +
                " SELECT room_id FROM bookings WHERE status != 'CANCELLED' " +
                " AND CURRENT_DATE BETWEEN check_in_date AND check_out_date " +
                ")";

        String setOccupiedSql = "UPDATE rooms SET status = 'OCCUPIED' WHERE status != 'OCCUPIED' AND id IN (" +
                " SELECT room_id FROM bookings WHERE status != 'CANCELLED' " +
                " AND CURRENT_DATE BETWEEN check_in_date AND check_out_date " +
                ")";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            int freed = stmt.executeUpdate(resetOccupiedSql);
            int occupied = stmt.executeUpdate(setOccupiedSql);
            logger.info("Status synchronization: Freed {}, Occupied {}", freed, occupied);
        } catch (SQLException e) {
            logger.error("Error synchronizing statuses: {}", e.getMessage());
        }
    }
}
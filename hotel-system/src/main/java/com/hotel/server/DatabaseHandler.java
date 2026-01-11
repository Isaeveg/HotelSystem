package com.hotel.server;

import com.hotel.common.Room;
import com.hotel.common.User;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    public static User loginUser(String username, String password) {
        String role = username.equalsIgnoreCase("admin") ? "ADMIN" : "CLIENT";
        return new User(1, username, role);
    }

    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(1, "101", "Standard", "250", "FREE"));
        rooms.add(new Room(2, "102", "Luxury", "550", "FREE"));
        return rooms;
    }
}
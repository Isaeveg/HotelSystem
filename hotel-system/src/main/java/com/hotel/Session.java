package com.hotel;

import com.hotel.common.User;

public class Session {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getClientId() {
        return currentUser != null ? currentUser.getClientId() : 0;
    }
}
package com.hotel;

import com.hotel.common.User;

/**
 * Manages the current user session.
 * <p>
 * Stores the currently logged-in {@link User}.
 * </p>
 */
public class Session {
    private static User currentUser;

    /**
     * Sets the current logged-in user.
     *
     * @param user the user to set
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Gets the current logged-in user.
     *
     * @return the current user, or null if no user is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Gets the client ID of the current user.
     *
     * @return the client ID, or 0 if no user is logged in or if the user is not a
     *         client
     */
    public static int getClientId() {
        return currentUser != null ? currentUser.getClientId() : 0;
    }
}
package com.hotel.common;

import java.io.Serializable;

/**
 * Represents a system user.
 */
public class User implements Serializable {
    private int id;
    private int clientId;
    private String email;
    private String role;

    /**
     * Constructs a new User.
     *
     * @param id       the user ID
     * @param clientId the associated client ID (if applicable)
     * @param email    the user's email
     * @param role     the user's role (e.g., ADMIN, CLIENT)
     */
    public User(int id, int clientId, String email, String role) {
        this.id = id;
        this.clientId = clientId;
        this.email = email;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
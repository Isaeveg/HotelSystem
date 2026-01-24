package com.hotel.common;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private int clientId;
    private String email;
    private String role;

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
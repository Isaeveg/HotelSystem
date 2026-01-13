package com.hotel.common;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String email; // Вместо username
    private String role;

    public User(int id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
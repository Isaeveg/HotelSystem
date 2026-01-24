package com.hotel.common;

import java.io.Serializable;

/**
 * Represents a client in the system.
 */
public class Client implements Serializable {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    /**
     * Constructs a new Client.
     *
     * @param id        the client ID
     * @param firstName the first name
     * @param lastName  the last name
     * @param email     the email address
     * @param phone     the phone number
     */
    public Client(int id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    /**
     * Gets the full name of the client.
     *
     * @return the first and last name concatenated
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
package com.hotel.common;

import java.io.Serializable;

/**
 * Represents a hotel entity.
 */
public class Hotel implements Serializable {
    private int id;
    private String name;
    private String city;

    /**
     * Constructs a new Hotel.
     *
     * @param id   the hotel ID
     * @param name the hotel name
     * @param city the city where the hotel is located
     */
    public Hotel(int id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return city + " - " + name;
    }
}
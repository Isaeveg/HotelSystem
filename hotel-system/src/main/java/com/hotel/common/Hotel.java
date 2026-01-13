package com.hotel.common;

import java.io.Serializable;

public class Hotel implements Serializable {
    private int id;
    private String name;
    private String city;

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
        return city + " - " + name; // Это будет отображаться в выпадающем списке
    }
}
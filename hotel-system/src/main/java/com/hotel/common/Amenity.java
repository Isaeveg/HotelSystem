package com.hotel.common;

import java.io.Serializable;

public class Amenity implements Serializable {
    private int id;
    private String name;
    private double price;

    public Amenity(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " (" + price + " PLN)";
    }
}
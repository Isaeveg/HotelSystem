package com.hotel.common;

import java.io.Serializable;

/**
 * Represents an amenity that can be added to a booking.
 * <p>
 * This class stores information about the amenity's ID, name, and price.
 * </p>
 */
public class Amenity implements Serializable {
    private int id;
    private String name;
    private double price;

    /**
     * Constructs a new Amenity.
     *
     * @param id    the amenity ID
     * @param name  the name of the amenity
     * @param price the price of the amenity
     */
    public Amenity(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    /**
     * Gets the amenity ID.
     *
     * @return the amenity ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name of the amenity.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the price of the amenity.
     *
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns a string representation of the amenity.
     *
     * @return the string "Name (Price PLN)"
     */
    @Override
    public String toString() {
        return name + " (" + price + " PLN)";
    }
}
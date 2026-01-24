package com.hotel.common;

import java.io.Serializable;

/**
 * Represents a hotel room.
 */
public class Room implements Serializable {
    private int id;
    private int hotelId;
    private String number;
    private String type;
    private String price;
    private String status;
    private String description;
    private String hotelName;

    /**
     * Constructs a new Room.
     *
     * @param id          the room ID
     * @param hotelId     the hotel ID
     * @param number      the room number
     * @param type        the room type (e.g., Single, Double)
     * @param price       the price per night
     * @param status      the current status of the room (e.g., FREE, OCCUPIED)
     * @param description a description of the room
     * @param hotelName   the name of the hotel including city
     */
    public Room(int id, int hotelId, String number, String type, String price, String status, String description,
            String hotelName) {
        this.id = id;
        this.hotelId = hotelId;
        this.number = number;
        this.type = type;
        this.price = price;
        this.status = status;
        this.description = description;
        this.hotelName = hotelName;
    }

    public int getId() {
        return id;
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
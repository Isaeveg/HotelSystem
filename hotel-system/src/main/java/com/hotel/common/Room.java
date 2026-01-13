package com.hotel.common;

import java.io.Serializable;

public class Room implements Serializable {
    private int id;
    private int hotelId; // <-- НОВОЕ ПОЛЕ
    private String number;
    private String type;
    private String price;
    private String status;
    private String description;
    // Опционально: название отеля для красоты в таблице
    private String hotelName;

    // Обновленный конструктор
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

    // Геттеры
    public int getId() {
        return id;
    }

    public int getHotelId() {
        return hotelId;
    } // <-- ГЕТТЕР

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
}
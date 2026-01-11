package com.hotel.common;

import java.io.Serializable;

public class Room implements Serializable {
    private int id;
    private String number;
    private String type;
    private double price;
    private String status;

    public Room(int id, String number, String type, double price, String status) {
        this.id = id;
        this.number = number;
        this.type = type;
        this.price = price;
        this.status = status;
    }

    public int getId() { return id; }
    public String getNumber() { return number; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "Pokój " + number + " (" + type + ") - " + price + " zł";
    }
}
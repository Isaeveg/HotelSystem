package com.hotel.common;
import java.io.Serializable;
public class Room implements Serializable {
    private int id;
    private String number;
    private String type;
    private String price;
    private String status;
    public Room(int id, String number, String type, String price, String status) {
        this.id = id; this.number = number; this.type = type; this.price = price; this.status = status;
    }
    public int getId() { return id; }
    public String getNumber() { return number; }
    public String getType() { return type; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
}
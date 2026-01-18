package com.hotel.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int clientId;
    private int roomId;
    private String clientEmail;
    private String roomNumber;
    private String checkInDate;
    private String checkOutDate;
    private String totalPrice;
    private String status;
    private List<Integer> amenityIds;

    public Booking(int id, int clientId, int roomId, String clientEmail, String roomNumber,
            String checkInDate, String checkOutDate, String totalPrice, String status,
            List<Integer> amenityIds) {
        this.id = id;
        this.clientId = clientId;
        this.roomId = roomId;
        this.clientEmail = clientEmail;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.amenityIds = amenityIds != null ? amenityIds : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Integer> getAmenityIds() {
        return amenityIds;
    }
}
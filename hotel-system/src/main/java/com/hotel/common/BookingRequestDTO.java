package com.hotel.common;

import java.io.Serializable;
import java.time.LocalDate;

public class BookingRequestDTO implements Serializable {
    private int userId;
    private int roomId;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public BookingRequestDTO(int userId, int roomId, LocalDate dateFrom, LocalDate dateTo) {
        this.userId = userId;
        this.roomId = roomId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public int getUserId() { return userId; }
    public int getRoomId() { return roomId; }
    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
}
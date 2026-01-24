package com.hotel.common;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Data Transfer Object for carrying booking request information.
 */
public class BookingRequestDTO implements Serializable {
    private int userId;
    private int roomId;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    /**
     * Constructs a new BookingRequestDTO.
     *
     * @param userId   the ID of the user request the booking
     * @param roomId   the ID of the room
     * @param dateFrom the start date
     * @param dateTo   the end date
     */
    public BookingRequestDTO(int userId, int roomId, LocalDate dateFrom, LocalDate dateTo) {
        this.userId = userId;
        this.roomId = roomId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public int getUserId() {
        return userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }
}
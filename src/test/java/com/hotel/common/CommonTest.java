package com.hotel.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonTest {

    @Test
    public void testUserCreation() {
        // Тест: Проверяем, что пользователь создается с правильными данными
        User user = new User(1, "admin", "ADMIN");

        Assertions.assertEquals(1, user.getId());
        Assertions.assertEquals("admin", user.getUsername());
        Assertions.assertEquals("ADMIN", user.getRole());
    }

    @Test
    public void testRoomPriceValidation() {
        // Тест: Проверяем логику данных комнаты
        Room room = new Room(10, "101", "Standard", 350.0, "FREE");

        Assertions.assertNotNull(room);
        Assertions.assertEquals(350.0, room.getPrice());
        Assertions.assertEquals("FREE", room.getStatus());
    }

    @Test
    public void testBookingDtoDates() {
        // Тест: Проверяем, что даты в DTO сохраняются
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate tomorrow = now.plusDays(1);

        BookingRequestDTO dto = new BookingRequestDTO(5, 10, now, tomorrow);

        Assertions.assertEquals(now, dto.getDateFrom());
        Assertions.assertTrue(dto.getDateTo().isAfter(dto.getDateFrom())); // Дата выезда должна быть после заезда
    }
}
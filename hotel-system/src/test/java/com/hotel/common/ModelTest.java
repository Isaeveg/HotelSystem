package com.hotel.common;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testUserSerialization() throws IOException, ClassNotFoundException {
        User originalUser = new User(1, 100, "test@example.com", "CLIENT");

        // Serialize
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(originalUser);
        out.close();

        // Deserialize
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        User deserializedUser = (User) in.readObject();

        assertNotNull(deserializedUser);
        assertEquals(originalUser.getId(), deserializedUser.getId());
        assertEquals(originalUser.getEmail(), deserializedUser.getEmail());
        assertEquals(originalUser.getRole(), deserializedUser.getRole());
    }

    @Test
    void testBookingSerialization() throws IOException, ClassNotFoundException {
        Booking originalBooking = new Booking(5, 10, 20, "client@test.com", "101",
                "2023-01-01", "2023-01-05", "400.0", "CONFIRMED", new ArrayList<>());

        // Serialize
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(originalBooking);
        out.close();

        // Deserialize
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        Booking deserializedBooking = (Booking) in.readObject();

        assertNotNull(deserializedBooking);
        assertEquals(originalBooking.getId(), deserializedBooking.getId());
        assertEquals(originalBooking.getRoomNumber(), deserializedBooking.getRoomNumber());
        assertEquals(originalBooking.getTotalPrice(), deserializedBooking.getTotalPrice());
    }
}

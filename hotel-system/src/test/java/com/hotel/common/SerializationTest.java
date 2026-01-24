package com.hotel.common;

import org.junit.jupiter.api.Test;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

class SerializationTest {

    @Test
    void testRequestSerialization() throws IOException, ClassNotFoundException {
        Request originalRequest = new Request(RequestType.LOGIN, "testUser");

        // Serialize
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(originalRequest);
        out.close();

        // Deserialize
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        Request deserializedRequest = (Request) in.readObject();

        assertNotNull(deserializedRequest);
        assertEquals(originalRequest.getType(), deserializedRequest.getType());
        assertEquals(originalRequest.getData(), deserializedRequest.getData());
    }

    @Test
    void testResponseSerialization() throws IOException, ClassNotFoundException {
        Response originalResponse = new Response(true, "Success", "Some Data");

        // Serialize
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(originalResponse);
        out.close();

        // Deserialize
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        Response deserializedResponse = (Response) in.readObject();

        assertNotNull(deserializedResponse);
        assertTrue(deserializedResponse.isSuccess());
        assertEquals("Success", deserializedResponse.getMessage());
        assertEquals("Some Data", deserializedResponse.getData());
    }
}

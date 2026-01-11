package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.Response;
import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    public Response sendRequest(Request request) {
        try (Socket socket = new Socket(HOST, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Błąd połączenia z serwerem", null);
        }
    }
}
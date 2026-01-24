package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.Response;
import java.io.*;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkClient {
    private static final Logger logger = LogManager.getLogger(NetworkClient.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    public static Response sendRequest(Request request) {
        try (Socket socket = new Socket(HOST, PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            logger.error("Network request error: ", e);
            return new Response(false, "Connection to server failed", null);
        }
    }
}
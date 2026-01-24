package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.Response;
import java.io.*;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles network communication with the server.
 * <p>
 * This class provides a static method to send requests to the server and
 * receive responses.
 * </p>
 */
public class NetworkClient {
    private static final Logger logger = LogManager.getLogger(NetworkClient.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    /**
     * Sends a request to the server and waits for a response.
     * <p>
     * Establishes a new socket connection for each request.
     * </p>
     *
     * @param request the request object to send
     * @return the response object received from the server
     */
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
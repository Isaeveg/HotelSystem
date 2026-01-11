package com.hotel.client;

import com.hotel.common.Request;
import com.hotel.common.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8189;
    private static final Logger logger = LogManager.getLogger(NetworkClient.class);

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private static NetworkClient instance;

    private NetworkClient() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            logger.info("Podłączono do serwera");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Nie udało się połączyć z serwerem");
        }
    }

    public static NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    public Response sendRequest(Request request) {
        try {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package com.hotel.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    private static final int PORT = 8189;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server hotela został włączony na porcie " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nowe połączenie klienta...");
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
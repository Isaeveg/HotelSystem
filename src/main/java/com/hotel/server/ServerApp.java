package com.hotel.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerApp {
    private static final int PORT = 8189;
    private static final Logger logger = LogManager.getLogger(ServerApp.class);

    public static void main(String[] args) {
        try {
            DatabaseHandler.getDbConnection();
            logger.info("Baza danych jest dostępna.");
        } catch (Exception e) {
            logger.error("Błąd bazy danych. Nie można uruchomić serwera.");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Serwer został pomyślnie uruchomiony na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Nowe połączenie od: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            logger.error("Krytyczny błąd serwera", e);
        }
    }
}
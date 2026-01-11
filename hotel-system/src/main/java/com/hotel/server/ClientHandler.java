package com.hotel.server;

import com.hotel.common.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Request request = (Request) in.readObject();
            Response response;

            switch (request.getType()) {
                case LOGIN:
                    // В твоем текущем фронте передается строка (логин)
                    String login = (String) request.getData();
                    System.out.println("Попытка входа: " + login);
                    
                    // Заглушка: админ или клиент
                    String role = login.equalsIgnoreCase("admin") ? "ADMIN" : "CLIENT";
                    response = new Response(true, "Авторизация успешна", role);
                    break;

                case GET_ROOMS:
                    List<Room> rooms = DatabaseHandler.getAllRooms();
                    response = new Response(true, "Список получен", rooms);
                    break;

                default:
                    response = new Response(false, "Неизвестный запрос", null);
            }

            out.writeObject(response);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
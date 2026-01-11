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
                    String[] credentials = (String[]) request.getData();
                    String login = credentials[0];
                    String password = credentials[1];
                    
                    System.out.println("Próba logowania: " + login);
                    
                    User user = DatabaseHandler.loginUser(login, password);
                    
                    if (user != null) {
                        response = new Response(true, "Успешно", user.getRole());
                    } else {
                        response = new Response(false, "Неверный логин или пароль", null);
                    }
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
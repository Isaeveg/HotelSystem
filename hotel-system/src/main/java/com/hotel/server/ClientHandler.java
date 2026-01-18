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
                        response = new Response(true, "Sukces", user.getRole());
                    } else {
                        response = new Response(false, "Błędny login lub hasło", null);
                    }
                    break;

                case GET_ROOMS:
                    List<Room> rooms = DatabaseHandler.getAllRooms();
                    response = new Response(true, "Lista pobrana", rooms);
                    break;

                case REGISTER:
                    String[] regData = (String[]) request.getData();
                    if (regData.length < 5) {
                        response = new Response(false, "Niekompletne dane", null);
                        break;
                    }

                    String regFirst = regData[0];
                    String regLast = regData[1];
                    String regEmail = regData[2];
                    String regPhone = regData[3];
                    String regPass = regData[4];

                    boolean isRegistered = DatabaseHandler.registerUser(regFirst, regLast, regEmail, regPhone, regPass);

                    if (isRegistered) {
                        response = new Response(true, "Konto utworzone pomyślnie!", null);
                    } else {
                        response = new Response(false, "Błąd rejestracji (email zajęty?)", null);
                    }
                    break;

                case GET_HOTELS:
                    List<Hotel> hotels = DatabaseHandler.getHotels();
                    response = new Response(true, "Lista hoteli", hotels);
                    break;

                case ADD_ROOM:
                    String[] roomData = (String[]) request.getData();

                    try {
                        int hId = Integer.parseInt(roomData[0]);
                        boolean success = DatabaseHandler.addRoom(hId, roomData[1], roomData[2], roomData[3],
                                roomData[4]);

                        if (success) {
                            response = new Response(true, "Pokój utworzony!", null);
                        } else {
                            response = new Response(false, "Błąd: numer może być zajęty w tym hotelu", null);
                        }
                    } catch (NumberFormatException e) {
                        response = new Response(false, "Nieprawidłowe ID hotelu", null);
                    }
                    break;

                case DELETE_ROOM:
                    String idStr = (String) request.getData();
                    try {
                        int roomId = Integer.parseInt(idStr);
                        boolean deleted = DatabaseHandler.deleteRoom(roomId);
                        if (deleted) {
                            response = new Response(true, "Pokój usunięty", null);
                        } else {
                            response = new Response(false, "Nie udało się usunąć pokoju", null);
                        }
                    } catch (NumberFormatException e) {
                        response = new Response(false, "Błędne ID pokoju", null);
                    }
                    break;

                case UPDATE_ROOM:
                    String[] updateData = (String[]) request.getData();
                    try {
                        int rId = Integer.parseInt(updateData[0]);
                        boolean updated = DatabaseHandler.updateRoom(rId, updateData[1], updateData[2], updateData[3],
                                updateData[4]);
                        if (updated) {
                            response = new Response(true, "Pokój zaktualizowany!", null);
                        } else {
                            response = new Response(false, "Błąd aktualizacji", null);
                        }
                    } catch (Exception e) {
                        response = new Response(false, "Błąd danych: " + e.getMessage(), null);
                    }
                    break;

                case GET_CLIENTS:
                    List<Client> clients = DatabaseHandler.getAllClients();
                    response = new Response(true, "Lista klientów", clients);
                    break;

                case ADD_CLIENT:
                    String[] addCData = (String[]) request.getData();
                    boolean added = DatabaseHandler.addClient(addCData[0], addCData[1], addCData[2], addCData[4],
                            addCData[3]);
                    response = new Response(added, added ? "Klient dodany" : "Błąd dodawania (email zajęty?)", null);
                    break;

                case DELETE_CLIENT:
                    int idToDelete = Integer.parseInt((String) request.getData());
                    boolean deletedC = DatabaseHandler.deleteClient(idToDelete);
                    response = new Response(deletedC, deletedC ? "Klient usunięty" : "Błąd usuwania", null);
                    break;

                case UPDATE_CLIENT:
                    String[] updCData = (String[]) request.getData();
                    boolean updatedC = DatabaseHandler.updateClient(
                            Integer.parseInt(updCData[0]),
                            updCData[1], updCData[2], updCData[3], updCData[4]);
                    response = new Response(updatedC, updatedC ? "Dane zaktualizowane" : "Błąd aktualizacji", null);
                    break;

                default:
                    response = new Response(false, "Nieznane zapytanie", null);
                    break;
            }

            out.writeObject(response);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.hotel.server;

import com.hotel.common.Request;
import com.hotel.common.Response;
import com.hotel.common.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            logger.info("Klient podłączony: " + clientSocket.getInetAddress());

            while (true) {
                try {
                    Request request = (Request) in.readObject();
                    Response response = handleRequest(request);

                    out.writeObject(response);
                    out.flush();

                } catch (ClassNotFoundException e) {
                    logger.info("Przybył nieznany obiekt");
                }
            }
        } catch (IOException e) {
            logger.info("Klient się rozłączył: " + clientSocket.getInetAddress());
        } finally {
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Response handleRequest(Request request) {
        switch (request.getType()) {
            case LOGIN:
                User u = (User) request.getData();
                logger.info("Próba logowania: " + u.getUsername());

                User dbUser = DatabaseHandler.loginUser(u.getUsername(), u.getPassword());

                if (dbUser != null) {
                    return new Response(true, "Logowanie zakończone sukcesem", dbUser);
                } else {
                    return new Response(false, "Nieprawidłowy login lub hasło", null);
                }

            case GET_ROOMS:
                logger.info("Klient poprosił o listę pokoi...");
                java.util.List<com.hotel.common.Room> rooms = DatabaseHandler.getAllRooms();
                return new Response(true, "Lista otrzymana", (java.io.Serializable) rooms);

            case BOOK_ROOM:
                logger.info("Otrzymano zapytanie dotyczące rezerwacji.");
                com.hotel.common.BookingRequestDTO bookingData = (com.hotel.common.BookingRequestDTO) request.getData();

                boolean booked = DatabaseHandler.createBooking(
                        bookingData.getUserId(),
                        bookingData.getRoomId(),
                        bookingData.getDateFrom(),
                        bookingData.getDateTo()
                );

                if (booked) {
                    return new Response(true, "Rezerwacja potwierdzona!", null);
                } else {
                    return new Response(false, "Błąd rezerwacji (może brak profilu klienta?)", null);
                }

            case ADD_ROOM:
                com.hotel.common.Room newRoom = (com.hotel.common.Room) request.getData();
                if (DatabaseHandler.addRoom(newRoom)) {
                    return new Response(true, "Pokój dodany!", null);
                } else {
                    return new Response(false, "Błąd dodawania (może numer już istnieje?)", null);
                }

            case DELETE_ROOM:
                int idToDelete = (int) request.getData();
                if (DatabaseHandler.deleteRoom(idToDelete)) {
                    return new Response(true, "Usunięto pomyślnie", null);
                } else {
                    return new Response(false, "Nie udało się usunąć", null);
                }

            case REGISTER:
                logger.info("Wniosek o rejestrację nowego użytkownika.");
                User newUser = (User) request.getData();

                boolean registered = DatabaseHandler.registerUser(newUser);

                if (registered) {
                    return new Response(true, "Rejestracja zakończona sukcesem! Teraz zaloguj się.", null);
                } else {
                    return new Response(false, "Login zajęty lub awaria bazy danych.", null);
                }

            default:
                return new Response(false, "Nieznana komenda", null);
        }
    }
}
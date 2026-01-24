package com.hotel.server;

import com.hotel.common.*;
import java.io.*;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles individual client connections in a separate thread.
 * <p>
 * This class implements {@link Runnable} and is responsible for processing
 * requests
 * sent by the client, interacting with the {@link DatabaseHandler} to perform
 * necessary operations, and sending back responses.
 * </p>
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    /**
     * Constructs a new ClientHandler for a given socket.
     *
     * @param socket the client socket
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Main execution loop for handling client requests.
     * <p>
     * Reads a {@link Request} object from the input stream, processes it based on
     * its type,
     * and writes a {@link Response} object to the output stream.
     * </p>
     */
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

                    logger.info("Login attempt: {}", login);

                    User user = DatabaseHandler.loginUser(login, password);

                    if (user != null) {
                        response = new Response(true, "Success", user);
                    } else {
                        response = new Response(false, "Invalid login or password", null);
                    }
                    break;

                case GET_ROOMS:
                    List<Room> rooms = DatabaseHandler.getAllRooms();
                    response = new Response(true, "List fetched", rooms);
                    break;

                case REGISTER:
                    String[] regData = (String[]) request.getData();
                    if (regData.length < 5) {
                        response = new Response(false, "Incomplete data", null);
                        break;
                    }

                    String regFirst = regData[0];
                    String regLast = regData[1];
                    String regEmail = regData[2];
                    String regPhone = regData[3];
                    String regPass = regData[4];

                    boolean isRegistered = DatabaseHandler.registerUser(regFirst, regLast, regEmail, regPhone, regPass);

                    if (isRegistered) {
                        response = new Response(true, "Account created successfully!", null);
                    } else {
                        response = new Response(false, "Registration error (email taken?)", null);
                    }
                    break;

                case GET_HOTELS:
                    List<Hotel> hotels = DatabaseHandler.getHotels();
                    response = new Response(true, "Hotel list", hotels);
                    break;

                case ADD_ROOM:
                    String[] roomData = (String[]) request.getData();

                    try {
                        int hId = Integer.parseInt(roomData[0]);
                        boolean success = DatabaseHandler.addRoom(hId, roomData[1], roomData[2], roomData[3],
                                roomData[4]);

                        if (success) {
                            response = new Response(true, "Room created!", null);
                        } else {
                            response = new Response(false, "Error: number might be taken in this hotel", null);
                        }
                    } catch (NumberFormatException e) {
                        response = new Response(false, "Invalid Hotel ID", null);
                    }
                    break;

                case DELETE_ROOM:
                    String idStr = (String) request.getData();
                    try {
                        int roomId = Integer.parseInt(idStr);
                        boolean deleted = DatabaseHandler.deleteRoom(roomId);
                        if (deleted) {
                            response = new Response(true, "Room deleted", null);
                        } else {
                            response = new Response(false, "Failed to delete room", null);
                        }
                    } catch (NumberFormatException e) {
                        response = new Response(false, "Invalid Room ID", null);
                    }
                    break;

                case UPDATE_ROOM:
                    String[] updateData = (String[]) request.getData();
                    try {
                        int rId = Integer.parseInt(updateData[0]);
                        boolean updated = DatabaseHandler.updateRoom(rId, updateData[1], updateData[2], updateData[3],
                                updateData[4]);
                        if (updated) {
                            response = new Response(true, "Room updated!", null);
                        } else {
                            response = new Response(false, "Update error", null);
                        }
                    } catch (Exception e) {
                        response = new Response(false, "Data error: " + e.getMessage(), null);
                    }
                    break;

                case GET_CLIENTS:
                    List<Client> clients = DatabaseHandler.getAllClients();
                    response = new Response(true, "Client list", clients);
                    break;

                case ADD_CLIENT:
                    String[] addCData = (String[]) request.getData();
                    boolean added = DatabaseHandler.addClient(addCData[0], addCData[1], addCData[2], addCData[4],
                            addCData[3]);
                    response = new Response(added, added ? "Client added" : "Adding error (email taken?)", null);
                    break;

                case DELETE_CLIENT:
                    int idToDelete = Integer.parseInt((String) request.getData());
                    boolean deletedC = DatabaseHandler.deleteClient(idToDelete);
                    response = new Response(deletedC, deletedC ? "Client deleted" : "Deleting error", null);
                    break;

                case UPDATE_CLIENT:
                    String[] updCData = (String[]) request.getData();
                    boolean updatedC = DatabaseHandler.updateClient(
                            Integer.parseInt(updCData[0]),
                            updCData[1], updCData[2], updCData[3], updCData[4]);
                    response = new Response(updatedC, updatedC ? "Data updated" : "Update error", null);
                    break;

                case GET_BOOKINGS:
                    List<Booking> bookings = DatabaseHandler.getAllBookings();
                    response = new Response(true, "Booking list", bookings);
                    break;

                case ADD_BOOKING:
                    Object[] bDataObj = (Object[]) request.getData();

                    int bClientId = Integer.parseInt((String) bDataObj[0]);
                    int bRoomId = Integer.parseInt((String) bDataObj[1]);
                    String bInDate = (String) bDataObj[2];
                    String bOutDate = (String) bDataObj[3];
                    String bPrice = (String) bDataObj[4];

                    String bStatus = (String) bDataObj[5];

                    @SuppressWarnings("unchecked")
                    List<Integer> amenityIds = (List<Integer>) bDataObj[6];

                    boolean bAdded = DatabaseHandler.addBooking(
                            bClientId,
                            bRoomId,
                            bInDate,
                            bOutDate,
                            bPrice,
                            bStatus,
                            amenityIds);

                    response = new Response(bAdded, bAdded ? "Booking added" : "Adding error", null);
                    break;

                case DELETE_BOOKING:
                    int bIdDel = Integer.parseInt((String) request.getData());
                    boolean bDel = DatabaseHandler.deleteBooking(bIdDel);
                    response = new Response(bDel, bDel ? "Booking deleted" : "Deleting error", null);
                    break;

                case UPDATE_BOOKING:
                    Object[] bUpd = (Object[]) request.getData();

                    int uId = Integer.parseInt(String.valueOf(bUpd[0]));
                    int uClientId = Integer.parseInt(String.valueOf(bUpd[1]));
                    int uRoomId = Integer.parseInt(String.valueOf(bUpd[2]));
                    String uIn = (String) bUpd[3];
                    String uOut = (String) bUpd[4];
                    String uPrice = (String) bUpd[5];
                    String uStatus = (String) bUpd[6];

                    @SuppressWarnings("unchecked")
                    List<Integer> uAmIds = (List<Integer>) bUpd[7];

                    boolean bUpdated = DatabaseHandler.updateBooking(
                            uId, uClientId, uRoomId, uIn, uOut, uPrice, uStatus, uAmIds);

                    response = new Response(bUpdated, bUpdated ? "Booking updated" : "Update error",
                            null);
                    break;

                case GET_AMENITIES:
                    List<Amenity> amenities = DatabaseHandler.getAllAmenities();
                    response = new Response(true, "Amenities list", amenities);
                    break;

                case GET_DASHBOARD:
                    DashboardData data = DatabaseHandler.getDashboardStats();
                    response = new Response(true, "Dashboard data", data);
                    break;

                case SEARCH_ROOMS:
                    Object[] searchParams = (Object[]) request.getData();
                    String city = (String) searchParams[0];
                    String dFromStr = (String) searchParams[1];
                    String dToStr = (String) searchParams[2];

                    try {
                        List<Room> searchResults = DatabaseHandler.searchFreeRooms(
                                city,
                                LocalDate.parse(dFromStr),
                                LocalDate.parse(dToStr));
                        response = new Response(true, "Search results", searchResults);
                    } catch (Exception e) {
                        response = new Response(false, "Date error or server error", null);
                    }
                    break;

                case ADD_FAVORITE:
                    Object[] favAdd = (Object[]) request.getData();
                    int faClientId = (Integer) favAdd[0];
                    int faRoomId = (Integer) favAdd[1];
                    boolean faAdded = DatabaseHandler.addFavorite(faClientId, faRoomId);
                    response = new Response(faAdded, faAdded ? "Added to favorites" : "Adding error", null);
                    break;

                case REMOVE_FAVORITE:
                    Object[] favRem = (Object[]) request.getData();
                    int frClientId = (Integer) favRem[0];
                    int frRoomId = (Integer) favRem[1];
                    boolean frRem = DatabaseHandler.removeFavorite(frClientId, frRoomId);
                    response = new Response(frRem, frRem ? "Removed from favorites" : "Removal error", null);
                    break;

                case GET_FAVORITES:
                    int fClientId = (Integer) request.getData();
                    List<Room> favorites = DatabaseHandler.getFavorites(fClientId);
                    response = new Response(true, "Favorites", favorites);
                    break;

                case GET_CLIENT_DETAILS:
                    int dClientId = (Integer) request.getData();
                    Client dClient = DatabaseHandler.getClientDetails(dClientId);
                    if (dClient != null) {
                        response = new Response(true, "Client data", dClient);
                    } else {
                        response = new Response(false, "Data not found", null);
                    }
                    break;

                default:
                    response = new Response(false, "Unknown request", null);
                    break;
            }

            out.writeObject(response);
            out.flush();

        } catch (Exception e) {
            logger.error("Error in client handler: ", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error closing socket: ", e);
            }
        }
    }
}
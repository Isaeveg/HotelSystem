package com.hotel;

import com.hotel.common.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller for the main admin dashboard view.
 * <p>
 * Manages the different tabs (Dashboard, Rooms, Reservations, Clients) and
 * their respective data tables.
 * Handles navigation, data loading from the server, and CRUD operations for
 * rooms, clients, and bookings.
 * </p>
 */
public class AdminController {
    private static final Logger logger = LogManager.getLogger(AdminController.class);
    @FXML
    private VBox viewDashboard, viewRooms, viewReservations, viewClients;
    @FXML
    private Button btnDash, btnRooms, btnRes, btnClients;

    @FXML
    private Label lblResToday;
    @FXML
    private Label lblIncomeMonth;
    @FXML
    private Label lblMaxPrice, lblMinPrice, lblAvgPrice;

    @FXML
    private TableView<DashboardData.ActivityEntry> dashboardTable;
    @FXML
    private TableColumn<DashboardData.ActivityEntry, String> colTime;
    @FXML
    private TableColumn<DashboardData.ActivityEntry, String> colDesc;
    @FXML
    private TableColumn<DashboardData.ActivityEntry, String> colStatus;

    @FXML
    private TextField searchRoomField;
    @FXML
    private TableView<Room> roomsTable;
    @FXML
    private TableColumn<Room, String> colHotel, colRoomNr, colRoomType, colRoomStatus;
    @FXML
    private TableColumn<Room, String> colRoomPrice;
    @FXML
    private TableColumn<Room, String> colRoomDesc;

    private ObservableList<Room> masterRoomData = FXCollections.observableArrayList();

    @FXML
    private TableView<Client> clientsTable;
    @FXML
    private TableColumn<Client, Integer> colClientId;
    @FXML
    private TableColumn<Client, String> colClientFirstName;
    @FXML
    private TableColumn<Client, String> colClientLastName;
    @FXML
    private TableColumn<Client, String> colClientEmail;
    @FXML
    private TextField searchClientField;

    private ObservableList<Client> masterClientData = FXCollections.observableArrayList();

    @FXML
    private TableView<Booking> bookingsTable;
    @FXML
    private TableColumn<Booking, Integer> colBookId;
    @FXML
    private TableColumn<Booking, String> colBookClient;
    @FXML
    private TableColumn<Booking, String> colBookRoom;
    @FXML
    private TableColumn<Booking, String> colBookIn;
    @FXML
    private TableColumn<Booking, String> colBookOut;
    @FXML
    private TableColumn<Booking, String> colBookStatus;
    @FXML
    private TableColumn<Booking, String> colBookPrice;
    @FXML
    private TextField searchBookingField;

    private ObservableList<Booking> masterBookingData = FXCollections.observableArrayList();

    /**
     * Initializes the controller. Sets up the tables and loads initial data.
     */
    @FXML
    public void initialize() {
        setupRoomsTable();
        setupClientsTable();
        setupBookingsTable();
        setupDashboardTable();

        loadRoomsFromServer();
        loadDashboardFromServer();
    }

    private void setupDashboardTable() {
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        dashboardTable.setPlaceholder(new Label("No recent activity"));
    }

    private void setupRoomsTable() {
        colHotel.setCellValueFactory(new PropertyValueFactory<>("hotelName"));
        colRoomNr.setCellValueFactory(new PropertyValueFactory<>("number"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRoomDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void setupClientsTable() {
        colClientId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colClientLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void setupBookingsTable() {
        colBookId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBookClient.setCellValueFactory(new PropertyValueFactory<>("clientEmail"));
        colBookRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colBookIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colBookOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colBookStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBookPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
    }

    private void loadDashboardFromServer() {
        Request req = new Request(RequestType.GET_DASHBOARD, null);
        Response resp = NetworkClient.sendRequest(req);

        if (resp.isSuccess() && resp.getData() instanceof DashboardData) {
            DashboardData data = (DashboardData) resp.getData();

            if (lblResToday != null) {
                lblResToday.setText(String.valueOf(data.getReservationsToday()));
            }
            if (lblIncomeMonth != null) {
                lblIncomeMonth.setText(String.format("%,.2f PLN", data.getIncomeMonth()));
            }

            if (dashboardTable != null) {
                dashboardTable.setItems(FXCollections.observableArrayList(data.getRecentActivities()));
            }
        } else {
            logger.error("Error fetching dashboard data: {}", resp.getMessage());
        }
    }

    private void loadRoomsFromServer() {
        Request req = new Request(RequestType.GET_ROOMS, null);
        Response resp = NetworkClient.sendRequest(req);
        if (resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Room> rooms = (List<Room>) resp.getData();
            masterRoomData.setAll(rooms);
            roomsTable.setItems(masterRoomData);
            calculateStats(rooms);
        }
    }

    private void loadClientsFromServer() {
        Request req = new Request(RequestType.GET_CLIENTS, null);
        Response resp = NetworkClient.sendRequest(req);
        if (resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Client> clients = (List<Client>) resp.getData();
            masterClientData.setAll(clients);
            clientsTable.setItems(masterClientData);
        } else {
            showError("Failed to fetch client list: " + resp.getMessage());
        }
    }

    private void loadBookingsFromServer() {
        Request req = new Request(RequestType.GET_BOOKINGS, null);
        Response resp = NetworkClient.sendRequest(req);
        if (resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Booking> list = (List<Booking>) resp.getData();
            masterBookingData.setAll(list);
            bookingsTable.setItems(masterBookingData);
        } else {
            showError("Failed to fetch bookings.");
        }
    }

    private void calculateStats(List<Room> rooms) {
        if (rooms.isEmpty()) {
            lblMaxPrice.setText("0.00 PLN");
            lblMinPrice.setText("0.00 PLN");
            lblAvgPrice.setText("0.00 PLN");
            return;
        }

        double max = rooms.stream().mapToDouble(r -> Double.parseDouble(r.getPrice())).max().orElse(0);
        double min = rooms.stream().mapToDouble(r -> Double.parseDouble(r.getPrice())).min().orElse(0);
        double avg = rooms.stream().mapToDouble(r -> Double.parseDouble(r.getPrice())).average().orElse(0);

        lblMaxPrice.setText(String.format("%.2f PLN", max));
        lblMinPrice.setText(String.format("%.2f PLN", min));
        lblAvgPrice.setText(String.format("%.2f PLN", avg));
    }

    @FXML
    protected void onSearchRoom() {
        String query = searchRoomField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            roomsTable.setItems(masterRoomData);
        } else {
            ObservableList<Room> filteredList = masterRoomData.stream()
                    .filter(room -> room.getHotelName() != null &&
                            room.getHotelName().toLowerCase().contains(query))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            roomsTable.setItems(filteredList);
        }
    }

    @FXML
    protected void onSearchClient() {
        String query = searchClientField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            clientsTable.setItems(masterClientData);
        } else {
            ObservableList<Client> filtered = masterClientData.stream()
                    .filter(c -> c.getEmail().toLowerCase().contains(query) ||
                            c.getFirstName().toLowerCase().contains(query) ||
                            c.getLastName().toLowerCase().contains(query))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            clientsTable.setItems(filtered);
        }
    }

    @FXML
    protected void onSearchBooking() {
        String query = searchBookingField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            bookingsTable.setItems(masterBookingData);
        } else {
            ObservableList<Booking> filtered = masterBookingData.stream()
                    .filter(b -> b.getClientEmail().toLowerCase().contains(query) ||
                            b.getRoomNumber().toLowerCase().contains(query) ||
                            b.getStatus().toLowerCase().contains(query))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            bookingsTable.setItems(filtered);
        }
    }

    @FXML
    protected void showDashboard() {
        switchView(viewDashboard);
        updateActiveButton(btnDash);
        loadDashboardFromServer();
        loadRoomsFromServer();
    }

    @FXML
    protected void showRooms() {
        switchView(viewRooms);
        updateActiveButton(btnRooms);
    }

    @FXML
    protected void showReservations() {
        switchView(viewReservations);
        loadBookingsFromServer();
        updateActiveButton(btnRes);
    }

    @FXML
    protected void showClients() {
        switchView(viewClients);
        loadClientsFromServer();
        updateActiveButton(btnClients);
    }

    private void updateActiveButton(Button activeBtn) {
        btnDash.getStyleClass().remove("menu-btn-active");
        btnRooms.getStyleClass().remove("menu-btn-active");
        btnRes.getStyleClass().remove("menu-btn-active");
        btnClients.getStyleClass().remove("menu-btn-active");
        activeBtn.getStyleClass().add("menu-btn-active");
    }

    private void switchView(VBox view) {
        viewDashboard.setVisible(false);
        viewRooms.setVisible(false);
        viewReservations.setVisible(false);
        viewClients.setVisible(false);
        view.setVisible(true);
    }

    @FXML
    protected void onLogout() {
        try {
            SceneManager.switchScene("login-view.fxml");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void onAddRoomClick() {
        try {
            SceneManager.openModal("admin-add-room.fxml", "Add New Room");
            loadRoomsFromServer();
        } catch (Exception e) {
            showError("Failed to open window: " + e.getMessage());
        }
    }

    @FXML
    protected void onDeleteRoomClick() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Select a room!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete room " + selectedRoom.getNumber() + "?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            Request req = new Request(RequestType.DELETE_ROOM, String.valueOf(selectedRoom.getId()));
            Response resp = NetworkClient.sendRequest(req);
            if (resp.isSuccess()) {
                loadRoomsFromServer();
            }
        }
    }

    @FXML
    protected void onEditRoomClick() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Select a room!");
            return;
        }
        try {
            SceneManager.openModal("admin-add-room.fxml", "Edit Room", (AdminAddRoomController controller) -> {
                controller.setRoomData(selectedRoom);
            });
            loadRoomsFromServer();
        } catch (Exception e) {
            showError("Editing error: " + e.getMessage());
        }
    }

    @FXML
    protected void onAddClientClick() {
        try {
            SceneManager.openModal("admin-add-client.fxml", "Add Client");
            loadClientsFromServer();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void onEditClientClick() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a client!");
            return;
        }
        try {
            SceneManager.openModal("admin-add-client.fxml", "Edit Client", (AdminAddClientController controller) -> {
                controller.setClientData(selected);
            });
            loadClientsFromServer();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void onDeleteClientClick() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a client!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete client " + selected.getEmail() + "?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            Request req = new Request(RequestType.DELETE_CLIENT, String.valueOf(selected.getId()));
            Response resp = NetworkClient.sendRequest(req);
            if (resp.isSuccess()) {
                loadClientsFromServer();
            }
        }
    }

    @FXML
    protected void onAddBookingClick() {
        try {
            SceneManager.openModal("admin-add-booking.fxml", "Add Booking");
            loadBookingsFromServer();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void onEditBookingClick() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a booking!");
            return;
        }
        try {
            SceneManager.openModal("admin-add-booking.fxml", "Edit Booking",
                    (AdminAddBookingController controller) -> {
                        controller.setBookingData(selected);
                    });
            loadBookingsFromServer();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void onDeleteBookingClick() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a booking!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete booking #" + selected.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            Request req = new Request(RequestType.DELETE_BOOKING, String.valueOf(selected.getId()));
            Response resp = NetworkClient.sendRequest(req);
            if (resp.isSuccess()) {
                loadBookingsFromServer();
            } else {
                showError(resp.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
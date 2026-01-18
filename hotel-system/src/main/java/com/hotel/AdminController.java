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

public class AdminController {
    @FXML
    private VBox viewDashboard, viewRooms, viewReservations, viewClients;
    @FXML
    private Button btnDash, btnRooms, btnRes, btnClients;
    @FXML
    private Label lblMaxPrice, lblMinPrice, lblAvgPrice;
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
    public void initialize() {
        setupRoomsTable();
        setupClientsTable();
        loadRoomsFromServer();
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

    private void calculateStats(List<Room> rooms) {
        if (rooms.isEmpty()) return;
        double max = rooms.stream().mapToDouble(Room::getPrice).max().orElse(0);
        double min = rooms.stream().mapToDouble(Room::getPrice).min().orElse(0);
        double avg = rooms.stream().mapToDouble(Room::getPrice).average().orElse(0);

        lblMaxPrice.setText(String.format("%.2f zł", max));
        lblMinPrice.setText(String.format("%.2f zł", min));
        lblAvgPrice.setText(String.format("%.2f zł", avg));
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
            showError("Nie udało się pobrać listy klientów: " + resp.getMessage());
        }
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
    protected void showDashboard() {
        switchView(viewDashboard);
        updateActiveButton(btnDash);
    }

    @FXML
    protected void showRooms() {
        switchView(viewRooms);
        updateActiveButton(btnRooms);
    }

    @FXML
    protected void showReservations() {
        switchView(viewReservations);
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

    @FXML
    protected void onAddRoomClick() {
        try {
            SceneManager.openModal("admin-add-room.fxml", "Dodaj nowy pokój");
            loadRoomsFromServer();
        } catch (Exception e) {
            showError("Nie udało się otworzyć okna dodawania pokoju: " + e.getMessage());
        }
    }

    @FXML
    protected void onDeleteRoomClick() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Wybierz pokój do usunięcia!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Czy na pewno chcesz usunąć pokój " + selectedRoom.getNumber() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            Request req = new Request(RequestType.DELETE_ROOM, String.valueOf(selectedRoom.getId()));
            Response resp = NetworkClient.sendRequest(req);
            if (resp.isSuccess()) {
                loadRoomsFromServer();
            } else {
                showError("Błąd usuwania: " + resp.getMessage());
            }
        }
    }

    @FXML
    protected void onEditRoomClick() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Wybierz pokój do edycji!");
            return;
        }
        try {
            SceneManager.openModal("admin-add-room.fxml", "Edytuj pokój", (AdminAddRoomController controller) -> {
                controller.setRoomData(selectedRoom);
            });
            loadRoomsFromServer();
        } catch (Exception e) {
            showError("Nie udało się otworzyć edycji: " + e.getMessage());
        }
    }

    @FXML
    protected void onAddClientClick() {
        try {
            SceneManager.openModal("admin-add-client.fxml", "Dodaj klienta");
            loadClientsFromServer();
        } catch (Exception e) {
            showError("Błąd otwierania okna: " + e.getMessage());
        }
    }

    @FXML
    protected void onEditClientClick() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Wybierz klienta do edycji!");
            return;
        }
        try {
            SceneManager.openModal("admin-add-client.fxml", "Edytuj klienta", (AdminAddClientController controller) -> {
                controller.setClientData(selected);
            });
            loadClientsFromServer();
        } catch (Exception e) {
            showError("Błąd edycji: " + e.getMessage());
        }
    }

    @FXML
    protected void onDeleteClientClick() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Wybierz klienta do usunięcia!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Czy usunąć klienta " + selected.getEmail() + "? \nTo usunie również konto użytkownika!", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            Request req = new Request(RequestType.DELETE_CLIENT, String.valueOf(selected.getId()));
            Response resp = NetworkClient.sendRequest(req);
            if (resp.isSuccess()) {
                loadClientsFromServer();
            } else {
                showError("Błąd usuwania: " + resp.getMessage());
            }
        }
    }

    @FXML
    protected void onLogout() {
        try {
            SceneManager.switchScene("login-view.fxml");
        } catch (Exception e) {
            showError("Błąd podczas wylogowywania: " + e.getMessage());
        }
    }

    private void switchView(VBox view) {
        viewDashboard.setVisible(false);
        viewRooms.setVisible(false);
        viewReservations.setVisible(false);
        viewClients.setVisible(false);
        view.setVisible(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
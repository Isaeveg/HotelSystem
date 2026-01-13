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
    private TextField searchRoomField;
    @FXML
    private TableView<Room> roomsTable;
    @FXML
    private TableColumn<Room, String> colHotel, colRoomNr, colRoomType, colRoomStatus;
    @FXML
    private TableColumn<Room, String> colRoomPrice;

    private ObservableList<Room> masterRoomData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupRoomsTable();
        loadRoomsFromServer();
    }

    private void setupRoomsTable() {
        colHotel.setCellValueFactory(new PropertyValueFactory<>("hotelName"));
        colRoomNr.setCellValueFactory(new PropertyValueFactory<>("number"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadRoomsFromServer() {
        Request req = new Request(RequestType.GET_ROOMS, null);
        Response resp = NetworkClient.sendRequest(req);
        if (resp.isSuccess()) {
            List<Room> rooms = (List<Room>) resp.getData();

            // Сохраняем все данные в master-список
            masterRoomData.setAll(rooms);
            // И показываем их в таблице
            roomsTable.setItems(masterRoomData);
        }
    }

    @FXML
    protected void onSearchRoom() {
        String query = searchRoomField.getText().toLowerCase().trim();

        if (query.isEmpty()) {
            // Если пусто, возвращаем полный список
            roomsTable.setItems(masterRoomData);
        } else {
            // Фильтруем список: ищем совпадение в названии отеля
            ObservableList<Room> filteredList = masterRoomData.stream()
                    .filter(room -> room.getHotelName() != null &&
                            room.getHotelName().toLowerCase().contains(query))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            roomsTable.setItems(filteredList);
        }
    }

    @FXML
    protected void showDashboard() {
        switchView(viewDashboard);
    }

    @FXML
    protected void showRooms() {
        switchView(viewRooms);
    }

    @FXML
    protected void showReservations() {
        switchView(viewReservations);
    }

    @FXML
    protected void showClients() {
        switchView(viewClients);
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Czy na pewno chcesz usunąć pokój " + selectedRoom.getNumber() + "?", ButtonType.YES, ButtonType.NO);
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
            e.printStackTrace();
            showError("Nie udało się otworzyć edycji: " + e.getMessage());
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
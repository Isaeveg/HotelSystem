package com.hotel;

import com.hotel.common.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.List;

public class AdminController {
    @FXML private VBox viewDashboard, viewRooms, viewReservations, viewClients;
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> colRoomNr, colRoomType, colRoomStatus;
    @FXML private TableColumn<Room, Double> colRoomPrice;

    @FXML
    public void initialize() {
        setupRoomsTable();
        loadRoomsFromServer();
    }

    private void setupRoomsTable() {
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
            roomsTable.setItems(FXCollections.observableArrayList(rooms));
        }
    }

    @FXML protected void showDashboard() { switchView(viewDashboard); }
    @FXML protected void showRooms() { switchView(viewRooms); }

    private void switchView(VBox view) {
        viewDashboard.setVisible(false);
        viewRooms.setVisible(false);
        view.setVisible(true);
    }
}
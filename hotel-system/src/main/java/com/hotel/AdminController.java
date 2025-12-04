package com.hotel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;

public class AdminController {

    @FXML private Button btnDash;
    @FXML private Button btnRooms;
    @FXML private Button btnRes;
    @FXML private Button btnClients;

    @FXML private VBox viewDashboard;
    @FXML private VBox viewRooms;
    @FXML private VBox viewReservations;
    @FXML private VBox viewClients;

    @FXML private TableView<LogEntry> dashboardTable;
    @FXML private TableColumn<LogEntry, String> colTime;
    @FXML private TableColumn<LogEntry, String> colDesc;
    @FXML private TableColumn<LogEntry, String> colStatus;

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> colRoomNr;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, String> colRoomPrice;
    @FXML private TableColumn<Room, String> colRoomCode;
    @FXML private TableColumn<Room, String> colRoomStatus;
    @FXML private TableColumn<Room, Void> colRoomAction;

    @FXML
    public void initialize() {
        showDashboard();
        setupDashboardTable();
        setupRoomsTable();
    }

    private void setupDashboardTable() {
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("desc"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        ObservableList<LogEntry> logs = FXCollections.observableArrayList(
            new LogEntry("10:45", "Nowa rezerwacja: Jan Kowalski (Pokój 101)", "Sukces"),
            new LogEntry("10:30", "Zameldowanie: Pokój 205", "Check-in"),
            new LogEntry("09:15", "Anulowanie rezerwacji #452", "Anulowano"),
            new LogEntry("08:50", "Płatność przyjęta: 350 zł (Anna Nowak)", "Opłacono")
        );
        dashboardTable.setItems(logs);
    }

    private void setupRoomsTable() {
        colRoomNr.setCellValueFactory(new PropertyValueFactory<>("number"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colRoomCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Магия добавления кнопок
        Callback<TableColumn<Room, Void>, TableCell<Room, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-action-edit");
                btnDelete.getStyleClass().add("btn-action-delete");
                
                btnEdit.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    System.out.println("Edycja pokoju: " + room.getNumber());
                });

                btnDelete.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(room);
                    System.out.println("Usunięto pokój: " + room.getNumber());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        };

        colRoomAction.setCellFactory(cellFactory);

        ObservableList<Room> rooms = FXCollections.observableArrayList(
            new Room("101", "Standard", "350 zł", "1234", "Wolny"),
            new Room("102", "Double", "500 zł", "5678", "Zajęty"),
            new Room("201", "Apartament", "1200 zł", "9999", "Sprzątanie")
        );
        roomsTable.setItems(rooms);
    }

    @FXML
    protected void showDashboard() { switchView(viewDashboard, btnDash); }
    @FXML
    protected void showRooms() { switchView(viewRooms, btnRooms); }
    @FXML
    protected void showReservations() { switchView(viewReservations, btnRes); }
    @FXML
    protected void showClients() { switchView(viewClients, btnClients); }

    @FXML
    protected void onAddRoomClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("admin-add-room.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 650);
        Stage stage = new Stage();
        stage.setTitle("Dodaj Pokój");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL); 
        stage.showAndWait();
    }

    private void switchView(VBox viewToShow, Button activeBtn) {
        viewDashboard.setVisible(false);
        viewRooms.setVisible(false);
        viewReservations.setVisible(false);
        viewClients.setVisible(false);
        viewToShow.setVisible(true);

        btnDash.getStyleClass().remove("menu-btn-active");
        btnRooms.getStyleClass().remove("menu-btn-active");
        btnRes.getStyleClass().remove("menu-btn-active");
        btnClients.getStyleClass().remove("menu-btn-active");
        activeBtn.getStyleClass().add("menu-btn-active");
    }

    @FXML
    protected void onLogout() throws IOException {
        Stage stage = (Stage) btnDash.getScene().getWindow();
        SceneManager.switchScene(stage, "login-view.fxml");
    }

    public static class LogEntry {
        private String time, desc, status;
        public LogEntry(String time, String desc, String status) {
            this.time = time; this.desc = desc; this.status = status;
        }
        public String getTime() { return time; }
        public String getDesc() { return desc; }
        public String getStatus() { return status; }
    }

    public static class Room {
        private String number, type, price, code, status;
        public Room(String number, String type, String price, String code, String status) {
            this.number = number; this.type = type; this.price = price; this.code = code; this.status = status;
        }
        public String getNumber() { return number; }
        public String getType() { return type; }
        public String getPrice() { return price; }
        public String getCode() { return code; }
        public String getStatus() { return status; }
    }
}
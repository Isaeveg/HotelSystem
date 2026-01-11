package com.hotel.client;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import com.hotel.common.Room;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdminController {
    private static final Logger logger = LogManager.getLogger(AdminController.class);

    @FXML private Button btnDash;
    @FXML private Button btnRooms;
    @FXML private Button btnRes;
    @FXML private Button btnClients;

    @FXML private VBox viewDashboard;
    @FXML private VBox viewRooms;
    @FXML private VBox viewReservations;
    @FXML private VBox viewClients;

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> colRoomNr;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, Double> colRoomPrice;
    @FXML private TableColumn<Room, String> colRoomStatus;

    @FXML
    public void initialize() {
        if (colRoomNr != null) colRoomNr.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (colRoomType != null) colRoomType.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (colRoomPrice != null) colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (colRoomStatus != null) colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        showRooms();
    }

    private void switchView(VBox viewToShow, Button activeBtn) {
        if (viewDashboard != null) viewDashboard.setVisible(false);
        if (viewRooms != null) viewRooms.setVisible(false);
        if (viewReservations != null) viewReservations.setVisible(false);
        if (viewClients != null) viewClients.setVisible(false);

        if (viewToShow != null) viewToShow.setVisible(true);

        removeActiveStyle(btnDash);
        removeActiveStyle(btnRooms);
        removeActiveStyle(btnRes);
        removeActiveStyle(btnClients);

        if (activeBtn != null) activeBtn.getStyleClass().add("menu-btn-active");
    }

    private void removeActiveStyle(Button btn) {
        if (btn != null) btn.getStyleClass().remove("menu-btn-active");
    }

    @FXML public void showDashboard() { switchView(viewDashboard, btnDash); }

    @FXML
    public void showRooms() {
        switchView(viewRooms, btnRooms);
        loadData();
    }

    @FXML public void showReservations() { switchView(viewReservations, btnRes); }
    @FXML public void showClients() { switchView(viewClients, btnClients); }

    @FXML
    public void loadData() {
        logger.info("Pobieranie danych z serwera...");
        Request req = new Request(RequestType.GET_ROOMS, null);
        Response resp = NetworkClient.getInstance().sendRequest(req);

        if (resp.isSuccess()) {
            List<Room> rooms = (List<Room>) resp.getData();
            if (roomsTable != null) {
                roomsTable.setItems(FXCollections.observableArrayList(rooms));
            }
        } else {
            logger.error("Błąd pobierania: " + resp.getMessage());
        }
    }

    @FXML
    public void onAddRoomClick() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Dodaj nowy pokój");
        dialog.setHeaderText("Wprowadź dane pokoju");

        ButtonType loginButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField numberField = new TextField();
        TextField priceField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Standard", "Double", "Apartment");
        typeBox.setValue("Standard");

        grid.add(new Label("Numer:"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(new Label("Typ:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Cena:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    double price = Double.parseDouble(priceField.getText());
                    return new Room(0, numberField.getText(), typeBox.getValue(), price, "FREE");
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Cena musi być liczbą!");
                    return null;
                }
            }
            return null;
        });

        Optional<Room> result = dialog.showAndWait();

        result.ifPresent(newRoom -> {
            Request req = new Request(RequestType.ADD_ROOM, newRoom);
            Response resp = NetworkClient.getInstance().sendRequest(req);

            if (resp.isSuccess()) {
                showAlert("Sukces", "Pokój dodany!");
                loadData();
            } else {
                showAlert("Błąd", resp.getMessage());
            }
        });
    }

    @FXML
    public void onLogout(ActionEvent event) {
        try {
            ((Node) event.getSource()).getScene().getWindow().hide();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login_view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Hotel System - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
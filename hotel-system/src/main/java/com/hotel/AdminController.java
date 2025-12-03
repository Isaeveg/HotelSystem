package com.hotel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.stage.Modality;

public class AdminController {

    @FXML private Button btnDash;
    @FXML private Button btnRooms;
    @FXML private Button btnRes;
    @FXML private Button btnClients;

    @FXML private VBox viewDashboard;
    @FXML private VBox viewRooms;
    @FXML private VBox viewReservations; 
    @FXML private VBox viewClients;      

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    protected void showDashboard() {
        switchView(viewDashboard, btnDash);
    }

    @FXML
    protected void showRooms() {
        switchView(viewRooms, btnRooms);
    }

    @FXML
    protected void showReservations() {
        switchView(viewReservations, btnRes);
    }

    @FXML
    protected void showClients() {
        switchView(viewClients, btnClients);
    }

    @FXML
    protected void onAddRoomClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("admin-add-room.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 450);
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
}
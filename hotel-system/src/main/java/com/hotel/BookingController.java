package com.hotel;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class BookingController {

    @FXML private Label roomInfoLabel;
    @FXML private Label priceInfoLabel;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TextField phoneField;
    @FXML private Button confirmBtn;

    public void setRoomData(String roomName, String price) {
        roomInfoLabel.setText(roomName);
        priceInfoLabel.setText("Cena: " + price + " / noc");
    }

    @FXML
    protected void onConfirm() {
        if (dateFrom.getValue() == null || dateTo.getValue() == null || phoneField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Uzupełnij daty i numer telefonu!");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText("Rezerwacja została wysłana! Opłacono pomyślnie.");
        alert.showAndWait();

        Stage stage = (Stage) confirmBtn.getScene().getWindow();
        stage.close();
    }
}
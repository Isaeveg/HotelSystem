package com.hotel;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class AdminAddRoomController {

    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextField roomPriceField;
    @FXML private Button saveBtn; 

    @FXML
    protected void onSave() {
        if (roomNumberField.getText().isEmpty() || 
            roomPriceField.getText().isEmpty() || 
            roomTypeCombo.getValue() == null) {
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Uzupełnij wszystkie pola!");
            alert.showAndWait();
            return;
        }

        System.out.println("Zapisuję pokój: " + roomNumberField.getText());
        
        closeModal();
    }

    @FXML
    protected void onCancel() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) roomNumberField.getScene().getWindow();
        stage.close();
    }
}
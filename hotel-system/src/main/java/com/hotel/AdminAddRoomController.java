package com.hotel;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class AdminAddRoomController {

    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextField roomPriceField;
    @FXML private TextField roomCodeField; 
    
    @FXML private CheckBox checkBreakfast;
    @FXML private CheckBox checkParking;
    @FXML private CheckBox checkWifi;
    @FXML private CheckBox checkAC;

    @FXML private Button saveBtn;

    @FXML
    protected void onSave() {
        if (roomNumberField.getText().isEmpty() || 
            roomPriceField.getText().isEmpty() || 
            roomCodeField.getText().isEmpty() || 
            roomTypeCombo.getValue() == null) {
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Uzupełnij wszystkie pola, w tym kod do drzwi!");
            alert.showAndWait();
            return;
        }

        StringBuilder features = new StringBuilder();
        if (checkBreakfast.isSelected()) features.append("Śniadanie, ");
        if (checkParking.isSelected()) features.append("Parking, ");
        if (checkWifi.isSelected()) features.append("Wi-Fi, ");
        if (checkAC.isSelected()) features.append("Klimatyzacja");

        System.out.println("Zapisano: " + roomNumberField.getText() + 
                           " | Kod: " + roomCodeField.getText() + 
                           " | Typ: " + roomTypeCombo.getValue() + 
                           " | Opcje: " + features.toString());
        
        closeModal();
    }

    @FXML
    protected void onCancel() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
}
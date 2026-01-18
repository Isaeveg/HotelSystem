package com.hotel;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UserProfileController {

    @FXML private Button closeBtn;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;

    @FXML
    public void initialize() {
        lblName.setText("Imię i Nazwisko: (Zalogowany użytkownik)");
        lblEmail.setText("Email: guest@example.com");
        lblPhone.setText("Telefon: +48 000 000 000");
    }

    @FXML
    protected void onClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
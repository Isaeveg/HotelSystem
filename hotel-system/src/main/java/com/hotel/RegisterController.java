package com.hotel;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;

    @FXML
    protected void onRegisterConfirm() {
        String name = nameField.getText();
        
        if(name.isEmpty()) {
            showAlert("Uzupełnij dane!");
            return;
        }

        showAlert("Sukces! Konto utworzone dla: " + name);
        try {
            goBackToLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onBackClick() throws IOException {
        goBackToLogin();
    }

    private void goBackToLogin() throws IOException {
        Stage stage = (Stage) nameField.getScene().getWindow();
        SceneManager.switchScene(stage, "login-view.fxml");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
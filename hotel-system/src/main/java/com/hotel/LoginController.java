package com.hotel;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    protected void onLoginButtonClick() {
        String login = loginField.getText().trim().toLowerCase();

        if (login.isEmpty()) {
            showAlert("Błąd", "Wpisz login lub email!");
            return;
        }

        try {
            Stage stage = (Stage) loginField.getScene().getWindow();
            if (login.equals("admin")) {
                SceneManager.switchScene(stage, "admin-view.fxml");
            } else {
                SceneManager.switchScene(stage, "client-view.fxml");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Błąd krytyczny", "Nie udało się załadować widoku: " + e.getMessage());
        }
    }

    @FXML
    protected void onRegisterButtonClick() throws IOException {
        Stage stage = (Stage) loginField.getScene().getWindow();
        SceneManager.switchScene(stage, "register-view.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
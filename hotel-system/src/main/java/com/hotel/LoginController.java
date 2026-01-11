package com.hotel;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField loginField;

    @FXML
    protected void onLoginButtonClick() {
        String login = loginField.getText().trim().toLowerCase();
        System.out.println("Попытка входа для: " + login); 

        if (login.isEmpty()) {
            showAlert("Błąd", "Wpisz login!");
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
            showAlert("Błąd", "Nie удалось загрузить вид: " + e.getMessage());
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        System.out.println("Переход на регистрацию");
        try {
            Stage stage = (Stage) loginField.getScene().getWindow();
            SceneManager.switchScene(stage, "register-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Błąd", "Nie удалось загрузить окно регистрации");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import com.hotel.common.User;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onLoginButtonClick() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Enter login and password!");
            return;
        }

        Request req = new Request(RequestType.LOGIN, new String[] { login, password });
        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            User user = (User) resp.getData();

            Session.setCurrentUser(user);

            try {
                Stage stage = (Stage) loginField.getScene().getWindow();
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    SceneManager.switchScene(stage, "admin-view.fxml");
                } else {
                    SceneManager.switchScene(stage, "client-view.fxml");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Error", resp != null ? resp.getMessage() : "No connection to server");
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        try {
            Stage stage = (Stage) loginField.getScene().getWindow();
            SceneManager.switchScene(stage, "register-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load registration window");
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
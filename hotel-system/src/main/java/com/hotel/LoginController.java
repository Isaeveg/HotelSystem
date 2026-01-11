package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
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
    if (login.isEmpty()) {
        showAlert("Błąd", "Wpisz login!");
        return;
    }

    Request req = new Request(RequestType.LOGIN, login);
    Response resp = NetworkClient.sendRequest(req);

    if (resp != null && resp.isSuccess()) {
        String role = (String) resp.getData(); 
        try {
            Stage stage = (Stage) loginField.getScene().getWindow();
            if ("ADMIN".equals(role)) {
                SceneManager.switchScene(stage, "admin-view.fxml");
            } else {
                SceneManager.switchScene(stage, "client-view.fxml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } else {
        showAlert("Błąd", resp != null ? resp.getMessage() : "Brak połączenia с serwerem");
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
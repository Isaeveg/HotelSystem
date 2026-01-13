package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField; // Не забудь импорт!
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passField; // Этого поля не было в коде, хотя в FXML оно есть

    @FXML
    protected void onRegisterConfirm() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Заполни все поля! (Uzupełnij dane)");
            return;
        }

        // Формируем запрос на сервер
        // Шлем массив строк: [email (как логин), пароль, имя]
        String[] registrationData = { email, password, name };
        Request request = new Request(RequestType.REGISTER, registrationData);

        // Отправляем
        Response response = NetworkClient.sendRequest(request);

        if (response != null && response.isSuccess()) {
            showAlert("Успех! " + response.getMessage());
            try {
                goBackToLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String errorMsg = (response != null) ? response.getMessage() : "Ошибка соединения";
            showAlert("Ошибка регистрации: " + errorMsg);
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
package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passField;

    @FXML
    protected void onRegisterConfirm() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showAlert("Uzupełnij wszystkie dane!");
            return;
        }

        String[] registrationData = { firstName, lastName, email, phone, password };
        Request request = new Request(RequestType.REGISTER, registrationData);

        Response response = NetworkClient.sendRequest(request);

        if (response != null && response.isSuccess()) {
            showAlert("Sukces! " + response.getMessage());
            try {
                goBackToLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String errorMsg = (response != null) ? response.getMessage() : "Błąd połączenia";
            showAlert("Błąd rejestracji: " + errorMsg);
        }
    }

    @FXML
    protected void onBackClick() throws IOException {
        goBackToLogin();
    }

    private void goBackToLogin() throws IOException {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneManager.switchScene(stage, "login-view.fxml");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
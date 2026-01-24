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

/**
 * Controller for the user registration view.
 * <p>
 * Handles collecting user input, validating data locally, and sending a
 * registration
 * request to the server.
 * </p>
 */
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

    /**
     * Handles the registration confirmation.
     * <p>
     * Validates input fields and sends a registration request.
     * </p>
     */
    @FXML
    protected void onRegisterConfirm() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showAlert("Fill in all fields!");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Invalid email format!");
            return;
        }

        if (!phone.matches("^\\+?[0-9]{9,15}$")) {
            showAlert("Invalid phone number! (e.g. +48123456789 or 123456789)");
            return;
        }

        String[] registrationData = { firstName, lastName, email, phone, password };
        Request request = new Request(RequestType.REGISTER, registrationData);

        Response response = NetworkClient.sendRequest(request);

        if (response != null && response.isSuccess()) {
            showAlert("Success! " + response.getMessage());
            try {
                goBackToLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String errorMsg = (response != null) ? response.getMessage() : "Connection error";
            showAlert("Registration error: " + errorMsg);
        }
    }

    /**
     * Handles the back button click.
     *
     * @throws IOException if navigation fails
     */
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
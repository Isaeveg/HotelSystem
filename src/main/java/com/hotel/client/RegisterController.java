package com.hotel.client;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import com.hotel.common.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RegisterController {
    private static final Logger logger = LogManager.getLogger(RegisterController.class);

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    protected void onRegisterConfirm() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Wypełnij wszystkie pola!");
            return;
        }

        User newUser = new User(login, password);
        Request request = new Request(RequestType.REGISTER, newUser);

        logger.info("Wysłanie wniosku o rejestrację...");
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            logger.info("Rejestracja zakończona sukcesem!");
            showError("Sukces! Teraz zaloguj się.");
            try {
                goBackToLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String msg = (response != null) ? response.getMessage() : "Błąd połączenia";
            showError("Błąd: " + msg);
        }
    }

    @FXML
    protected void onBackClick() throws IOException {
        goBackToLogin();
    }

    private void goBackToLogin() throws IOException {
        Stage stage = (Stage) loginField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login_view.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
    }

    private void showError(String text) {
        if (errorLabel != null) {
            errorLabel.setText(text);
            errorLabel.setVisible(true);
        } else {
            System.err.println("GUI Error: " + text);
        }
    }
}
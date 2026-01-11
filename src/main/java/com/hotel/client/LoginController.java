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

public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    protected void onLoginClick() {
        String login = usernameField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Wprowadź login i hasło!");
            return;
        }

        User userCredentials = new User(login, password);
        Request request = new Request(RequestType.LOGIN, userCredentials);

        logger.info("Wysyłanie zapytania do serwera...");
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            User loggedUser = (User) response.getData();
            ClientSession.getInstance().setCurrentUser(loggedUser);
            logger.info("Logowanie zakończone sukcesem! Rola: " + loggedUser.getRole());

            try {
                usernameField.getScene().getWindow().hide();

                String viewFile = "";
                String title = "";

                if ("ADMIN".equals(loggedUser.getRole())) {
                    viewFile = "/admin_view.fxml";
                    title = "Hotel System - ADMIN PANEL";
                } else {
                    viewFile = "/client_view.fxml";
                    title = "Hotel System - Booking";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(viewFile));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(new Scene(root, 900, 600));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showError("Błąd otwierania okna: " + e.getMessage());
            }
        } else {
            String msg = (response != null) ? response.getMessage() : "Błąd połączenia";
            showError(msg);
        }
    }

    @FXML
    protected void onRegisterLinkClick() {
        logger.info("Przejście do okna rejestracji");
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register_view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Nie udało się otworzyć okna rejestracji: " + e.getMessage());
        }
    }

    private void showError(String text) {
        if (errorLabel != null) {
            errorLabel.setText(text);
            errorLabel.setVisible(true);
        } else {
            logger.error("ERROR LABEL NOT FOUND IN FXML: " + text);
        }
    }
}
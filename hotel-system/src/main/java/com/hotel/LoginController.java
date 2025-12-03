package com.hotel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
            if (login.equals("admin")) {
                showAlert("Info", "Panel Admina"); 
                // loadScene("admin-view.fxml");
            } else {
                loadScene("client-view.fxml");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Błąd krytyczny", "Nie udało się załadować widoku: " + e.getMessage());
        }
    }

    @FXML
    protected void onRegisterButtonClick() throws IOException {
        loadScene("register-view.fxml");
    }

 
    private void loadScene(String fxmlFile) throws IOException {
    
        Stage stage = (Stage) loginField.getScene().getWindow();
        double width = stage.getScene().getWidth();
        double height = stage.getScene().getHeight();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlFile));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, width, height);
        
        stage.setScene(scene);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
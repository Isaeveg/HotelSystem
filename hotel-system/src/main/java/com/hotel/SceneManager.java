package com.hotel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SceneManager {
    public static void switchScene(Stage stage, String fxmlFile) throws IOException {
        double width = stage.getScene().getWidth();
        double height = stage.getScene().getHeight();

        URL resource = App.class.getResource(fxmlFile);
        if (resource == null) {
            throw new IOException("Файл не найден в ресурсах: " + fxmlFile);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
    }
}
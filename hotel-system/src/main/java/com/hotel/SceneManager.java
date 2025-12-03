package com.hotel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
    public static void switchScene(Stage stage, String fxmlFile) throws IOException {
        double width = stage.getScene().getWidth();
        double height = stage.getScene().getHeight();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlFile));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
    }
}
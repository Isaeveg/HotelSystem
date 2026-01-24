package com.hotel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class SceneManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(Stage stage, String fxmlFile) throws IOException {
        double width = stage.getScene().getWidth();
        double height = stage.getScene().getHeight();

        URL resource = App.class.getResource(fxmlFile);
        if (resource == null) {
            throw new IOException("File not found in resources: " + fxmlFile);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
    }

    public static void switchScene(String fxmlFile) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set. Call setPrimaryStage() first.");
        }
        switchScene(primaryStage, fxmlFile);
    }

    public static <T> T openModal(String fxmlFile, String title) throws IOException {
        return openModal(fxmlFile, title, null);
    }

    public static <T> T openModal(String fxmlFile, String title, Consumer<T> controllerSetup) throws IOException {
        URL resource = App.class.getResource(fxmlFile);
        if (resource == null)
            throw new IOException("File not found: " + fxmlFile);

        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Parent root = fxmlLoader.load();

        T controller = fxmlLoader.getController();
        if (controllerSetup != null) {
            controllerSetup.accept(controller);
        }

        Stage modalStage = new Stage();
        modalStage.setTitle(title);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(primaryStage);
        modalStage.setScene(new Scene(root));
        modalStage.showAndWait();

        return controller;
    }
}
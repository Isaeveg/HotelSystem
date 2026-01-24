package com.hotel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Manages JavaFX scenes and stages.
 * <p>
 * Provides utility methods to switch scenes and open modal windows.
 * </p>
 */
public class SceneManager {
    private static Stage primaryStage;

    /**
     * Sets the primary stage of the application.
     *
     * @param stage the primary stage
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Switches the scene on a specific stage.
     *
     * @param stage    the stage to switch scene on
     * @param fxmlFile the FXML file of the new scene
     * @throws IOException if the FXML file cannot be loaded
     */
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

    /**
     * Switches the scene on the primary stage.
     *
     * @param fxmlFile the FXML file of the new scene
     * @throws IOException if the FXML file cannot be loaded
     */
    public static void switchScene(String fxmlFile) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set. Call setPrimaryStage() first.");
        }
        switchScene(primaryStage, fxmlFile);
    }

    /**
     * Opens a modal window.
     *
     * @param fxmlFile the FXML file for the modal content
     * @param title    the title of the modal window
     * @param <T>      the type of the controller
     * @return the controller of the loaded FXML
     * @throws IOException if the FXML file cannot be loaded
     */
    public static <T> T openModal(String fxmlFile, String title) throws IOException {
        return openModal(fxmlFile, title, null);
    }

    /**
     * Opens a modal window with a controller setup callback.
     *
     * @param fxmlFile        the FXML file for the modal content
     * @param title           the title of the modal window
     * @param controllerSetup a consumer to configure the controller before showing
     *                        the window
     * @param <T>             the type of the controller
     * @return the controller of the loaded FXML
     * @throws IOException if the FXML file cannot be loaded
     */
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
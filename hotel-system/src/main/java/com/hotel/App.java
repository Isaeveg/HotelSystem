package com.hotel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Main application entry point for the Hotel System Client.
 * <p>
 * This class extends {@link Application} and is responsible for setting up the
 * primary stage
 * and loading the initial login scene.
 * </p>
 */
public class App extends Application {

    /**
     * Starts the JavaFX application.
     * <p>
     * Sets the primary stage in {@link SceneManager} and loads the login view.
     * </p>
     *
     * @param stage the primary stage for this application
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setPrimaryStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main method to launch the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
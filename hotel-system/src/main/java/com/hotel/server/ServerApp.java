package com.hotel.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ServerApp extends Application {
    private static final int PORT = 8189;
    private static final Logger logger = LogManager.getLogger(ServerApp.class);

    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private TextArea logArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hotel System Server");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(400);

        Button startButton = new Button("Start Server");
        Button stopButton = new Button("Stop Server");
        stopButton.setDisable(true);

        startButton.setOnAction(e -> {
            startServer();
            startButton.setDisable(true);
            stopButton.setDisable(false);
        });

        stopButton.setOnAction(e -> {
            stopServer();
            startButton.setDisable(false);
            stopButton.setDisable(true);
        });

        VBox root = new VBox(10, startButton, stopButton, logArea);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> stopServer());
        primaryStage.show();
    }

    private void startServer() {
        if (isRunning)
            return;

        isRunning = true;
        log("Starting server on port " + PORT + "...");

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                log("Server started successfully.");
                DatabaseHandler.checkAndVerifyBookings();

                while (isRunning) {
                    try {
                        Socket socket = serverSocket.accept();
                        log("New client connection...");
                        new Thread(new ClientHandler(socket)).start();
                    } catch (IOException e) {
                        if (isRunning)
                            log("Error accepting connection: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                log("Could not listen on port " + PORT + ": " + e.getMessage());
            }
        }).start();
    }

    private void stopServer() {
        if (!isRunning)
            return;

        isRunning = false;
        log("Stopping server...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing server: " + e.getMessage());
        }
        log("Server stopped.");
    }

    private void log(String message) {

        logger.info(message);

        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }
}
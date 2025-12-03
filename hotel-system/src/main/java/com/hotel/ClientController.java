package com.hotel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientController {

    @FXML
    private FlowPane hotelsContainer;

    @FXML
    public void initialize() {
        addHotelCard("Grand Hotel Central", "350 zł", "#e0e0e0");
        addHotelCard("Sea Resort Spa", "620 zł", "#e0f7fa");
        addHotelCard("Hostel Student", "80 zł", "#fff3e0");
        addHotelCard("Mountain View", "450 zł", "#e8f5e9");
    }

    private void addHotelCard(String name, String price, String colorHex) {
        VBox card = new VBox();
        
        card.prefWidthProperty().bind(hotelsContainer.widthProperty().subtract(40).divide(2));
        
        card.setPrefHeight(320); 
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setPrefHeight(180);
        imagePlaceholder.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 8 0 0;");
        imagePlaceholder.setAlignment(Pos.CENTER);
        Label iconLabel = new Label("Hotel Icon"); 
        imagePlaceholder.getChildren().add(iconLabel);

        VBox body = new VBox(10);
        body.setPadding(new Insets(15));
        
        Label title = new Label(name);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#333333"));

        Label priceLabel = new Label(price);
        priceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        priceLabel.setTextFill(Color.web("#333333"));

        Button viewBtn = new Button("Wybierz >");
        viewBtn.setStyle("-fx-background-color: #0071c2; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        
        viewBtn.setOnAction(e -> {
            System.out.println("Selected: " + name);
        });

        body.getChildren().addAll(title, priceLabel, viewBtn);
        card.getChildren().addAll(imagePlaceholder, body);

        hotelsContainer.getChildren().add(card);
    }

    @FXML
    protected void onLogoutClick() throws IOException {
        Stage stage = (Stage) hotelsContainer.getScene().getWindow();
        
        double width = stage.getScene().getWidth();
        double height = stage.getScene().getHeight();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login-view.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
    }
}
package com.hotel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientController {

    @FXML private FlowPane hotelsContainer;
    @FXML private FlowPane favContainer; 
    @FXML private VBox favSection;       
    
    @FXML private ScrollPane listView;
    @FXML private ScrollPane detailsView;
    
    @FXML private Label detailTitle;
    @FXML private Label detailPrice;
    @FXML private VBox detailImageBox;
    @FXML private VBox detailRoomsContainer;

    @FXML
    public void initialize() {
        addHotelCard("Grand Hotel Central", "350 zł", "#e0e0e0");
        addHotelCard("Sea Resort Spa", "620 zł", "#e0f7fa");
        addHotelCard("Hostel Student", "80 zł", "#fff3e0");
        addHotelCard("Mountain View", "450 zł", "#e8f5e9");
        addHotelCard("City Center", "200 zł", "#f3e5f5"); 
    }

    private void addHotelCard(String name, String price, String colorHex) {
        VBox card = new VBox();
        card.setPrefWidth(300);  
        card.setPrefHeight(340); 
        card.getStyleClass().add("hotel-card");

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setPrefHeight(160);
        imagePlaceholder.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 8 0 0;");
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.getChildren().add(new Label("Hotel Icon"));

        VBox body = new VBox(5); 
        body.setPadding(new Insets(15));
        VBox.setVgrow(body, Priority.ALWAYS); 
        
        Label title = new Label(name);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#333333"));
        title.setWrapText(true); 

        Label priceLabel = new Label(price);
        priceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        priceLabel.setTextFill(Color.web("#333333"));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(0, 0, 0, 0)); 

        Button viewBtn = new Button("Wybierz");
        viewBtn.getStyleClass().add("btn-card-action");
        viewBtn.setOnAction(e -> openDetails(name, price, colorHex));

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button starBtn = new Button("★");
        starBtn.getStyleClass().add("btn-star");
        
        starBtn.setOnAction(e -> {
            boolean isFav = starBtn.getStyleClass().contains("btn-star-active");
            if (isFav) {
                starBtn.getStyleClass().remove("btn-star-active");
                favContainer.getChildren().remove(card);
                hotelsContainer.getChildren().add(card); 
            } else {
                starBtn.getStyleClass().add("btn-star-active");
                hotelsContainer.getChildren().remove(card);
                favContainer.getChildren().add(card);
            }
            checkFavSectionVisibility();
        });

        actions.getChildren().addAll(viewBtn, hSpacer, starBtn);
        body.getChildren().addAll(title, priceLabel, spacer, actions);
        card.getChildren().addAll(imagePlaceholder, body);
        hotelsContainer.getChildren().add(card);
    }

    private void checkFavSectionVisibility() {
        boolean hasFavorites = !favContainer.getChildren().isEmpty();
        favSection.setVisible(hasFavorites);
        favSection.setManaged(hasFavorites);
    }

    private void openDetails(String name, String price, String colorHex) {
        detailTitle.setText(name);
        detailPrice.setText(price);
        detailImageBox.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 4;");
        listView.setVisible(false);
        detailsView.setVisible(true);
    }

    @FXML
    protected void onBackToList() {
        detailsView.setVisible(false);
        listView.setVisible(true);
    }

    @FXML
    protected void onOpenFilters() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("filter-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 500);
        Stage stage = new Stage();
        stage.setTitle("Filtry");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    protected void onLogoutClick() throws IOException {
        Stage stage = (Stage) hotelsContainer.getScene().getWindow();
        SceneManager.switchScene(stage, "login-view.fxml");
    }
}
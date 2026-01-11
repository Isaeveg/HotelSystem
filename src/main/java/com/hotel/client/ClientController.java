package com.hotel.client;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import com.hotel.common.Room;
import com.hotel.common.BookingRequestDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ClientController {

    @FXML private FlowPane hotelsContainer;
    @FXML private FlowPane favContainer;
    @FXML private VBox favSection;

    @FXML private ScrollPane listView;
    @FXML private ScrollPane detailsView;

    @FXML private Label statusLabel;

    @FXML private Label detailTitle;
    @FXML private Label detailPrice;
    @FXML private VBox detailImageBox;
    @FXML private VBox detailRoomsContainer;

    @FXML
    public void initialize() {
        loadRooms();
    }

    private void loadRooms() {
        statusLabel.setText("Ładowanie danych...");

        Request req = new Request(RequestType.GET_ROOMS, null);
        Response resp = NetworkClient.getInstance().sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            List<Room> rooms = (List<Room>) resp.getData();
            hotelsContainer.getChildren().clear();

            for (Room room : rooms) {
                addRoomCard(room);
            }
            statusLabel.setText("Dostępne obiekty:");
        } else {
            statusLabel.setText("Błąd ładowania danych.");
        }
    }

    private void addRoomCard(Room room) {
        String name = "Pokój " + room.getNumber() + " (" + room.getType() + ")";
        String price = room.getPrice() + " zł";
        String colorHex = room.getStatus().equals("FREE") ? "#d4edda" : "#f8d7da";

        VBox card = new VBox();
        card.setPrefWidth(300);
        card.setPrefHeight(340);
        card.getStyleClass().add("hotel-card");

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setPrefHeight(160);
        imagePlaceholder.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 8 0 0;");
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.getChildren().add(new Label(room.getType().substring(0, 1)));

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

        Button viewBtn = new Button("Wybierz");
        viewBtn.getStyleClass().add("btn-card-action");
        viewBtn.setOnAction(e -> openDetails(room, colorHex));

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

    private void openDetails(Room room, String colorHex) {
        detailTitle.setText("Pokój " + room.getNumber() + " - " + room.getType());
        detailPrice.setText(room.getPrice() + " zł / noc");
        detailImageBox.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 4;");

        detailRoomsContainer.getChildren().clear();

        addBookingOption(room);

        listView.setVisible(false);
        detailsView.setVisible(true);
    }

    private void addBookingOption(Room room) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 15;");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        Label typeLbl = new Label("Oferta standardowa");
        typeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label descLbl = new Label("Status: " + room.getStatus());
        descLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        info.getChildren().addAll(typeLbl, descLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox action = new VBox(5);
        action.setAlignment(Pos.CENTER_RIGHT);
        Label priceLbl = new Label(room.getPrice() + " zł");
        priceLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        Button bookBtn = new Button("Zarezerwuj");
        bookBtn.getStyleClass().add("btn-choose");

        bookBtn.setOnAction(e -> handleBooking(room));

        action.getChildren().addAll(priceLbl, bookBtn);

        row.getChildren().addAll(info, spacer, action);
        detailRoomsContainer.getChildren().add(row);
    }

    private void handleBooking(Room room) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate tomorrow = today.plusDays(1);

        if (ClientSession.getInstance().getCurrentUser() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Błąd: Nie jesteś zalogowany!");
            alert.showAndWait();
            return;
        }

        int currentUserId = ClientSession.getInstance().getCurrentUser().getId();

        BookingRequestDTO bookingDto = new BookingRequestDTO(
                currentUserId,
                room.getId(),
                today,
                tomorrow
        );

        Request req = new Request(RequestType.BOOK_ROOM, bookingDto);
        Response resp = NetworkClient.getInstance().sendRequest(req);

        if (resp.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sukces");
            alert.setHeaderText(null);
            alert.setContentText("✅ " + resp.getMessage());
            alert.showAndWait();
            onBackToList();
            loadRooms();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setContentText("❌ " + resp.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void onBackToList() {
        detailsView.setVisible(false);
        listView.setVisible(true);
    }

    @FXML
    public void onLogoutClick(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login_view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setTitle("Hotel System - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
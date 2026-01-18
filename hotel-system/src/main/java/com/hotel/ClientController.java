package com.hotel;

import com.hotel.common.*;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ClientController {

    @FXML
    private FlowPane hotelsContainer;
    @FXML
    private FlowPane favContainer;
    @FXML
    private VBox favSection;

    @FXML
    private ScrollPane listView;
    @FXML
    private ScrollPane detailsView;

    @FXML
    private Label detailTitle;
    @FXML
    private Label detailPrice;
    @FXML
    private VBox detailImageBox;
    @FXML
    private VBox detailRoomsContainer;

    @FXML
    public void initialize() {
        checkFavSectionVisibility();
        loadRooms();
    }

    private void loadRooms() {
        Request req = new Request(RequestType.GET_ROOMS, null);
        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            List<Room> rooms = (List<Room>) resp.getData();
            hotelsContainer.getChildren().clear();

            for (Room room : rooms) {
                String colorHex = room.getStatus().equals("FREE") ? "#e8f5e9" : "#f3e5f5";
                addHotelCard(room, colorHex);
            }
        } else {
            System.err.println("Błąd ładowania pokoi: " + (resp != null ? resp.getMessage() : "Brak połączenia"));
        }
    }

    private void addHotelCard(Room room, String colorHex) {
        String name = "Pokój " + room.getNumber() + " (" + room.getType() + ")";
        String priceStr = room.getPrice() + " zł";

        VBox card = new VBox();
        card.setPrefWidth(300);
        card.setPrefHeight(340);
        card.getStyleClass().add("hotel-card");

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setPrefHeight(160);
        imagePlaceholder.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 8 0 0;");
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.getChildren().add(new Label(room.getType()));

        VBox body = new VBox(5);
        body.setPadding(new Insets(15));
        VBox.setVgrow(body, Priority.ALWAYS);

        Label title = new Label(name);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#333333"));
        title.setWrapText(true);

        Label priceLabel = new Label(priceStr);
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
        detailTitle.setText("Pokój " + room.getNumber());
        detailPrice.setText(room.getPrice() + " zł / noc");
        detailImageBox.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 4;");

        detailRoomsContainer.getChildren().clear();

        addRoomVariant(room.getType(), room.getPrice() + " zł", "Status: " + room.getStatus());

        listView.setVisible(false);
        detailsView.setVisible(true);
    }

    private void addRoomVariant(String type, String price, String desc) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 15;");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        Label typeLbl = new Label(type);
        typeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        info.getChildren().addAll(typeLbl, descLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox action = new VBox(5);
        action.setAlignment(Pos.CENTER_RIGHT);
        Label priceLbl = new Label(price);
        priceLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        Button bookBtn = new Button("Wybierz");
        bookBtn.getStyleClass().add("btn-choose");
        bookBtn.setOnAction(e -> openBookingModal(type, price));

        action.getChildren().addAll(priceLbl, bookBtn);

        row.getChildren().addAll(info, spacer, action);
        detailRoomsContainer.getChildren().add(row);
    }

    private void openBookingModal(String type, String price) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("booking-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 550);

            BookingController controller = fxmlLoader.getController();
            controller.setRoomData(type, price);

            Stage stage = new Stage();
            stage.setTitle("Rezerwacja");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onBackToList() {
        detailsView.setVisible(false);
        listView.setVisible(true);
    }

    @FXML
    protected void onLogoutClick(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchScene(stage, "login-view.fxml");
    }

    @FXML
    protected void onOpenProfile() {
        try {
            SceneManager.openModal("user-profile.fxml", "Profil użytkownika");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onOpenFilters() {
        try {
            SceneManager.openModal("filter-view.fxml", "Filtry");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.hotel;

import com.hotel.common.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientController {

    @FXML
    private FlowPane hotelsContainer;
    @FXML
    private ScrollPane listView, detailsView;
    @FXML
    private Label detailTitle, detailPrice, sectionTitle;
    @FXML
    private VBox detailImageBox, detailRoomsContainer;
    @FXML
    private Button btnMain, btnFavs, btnMyRes;

    private List<Room> allRooms = new ArrayList<>();
    private List<Room> favoriteRooms = new ArrayList<>();
    private List<Room> myReservations = new ArrayList<>();

    @FXML
    public void initialize() {
        loadRooms();
    }

    private void loadRooms() {
        Request req = new Request(RequestType.GET_ROOMS, null);
        Response resp = NetworkClient.sendRequest(req);
        if (resp != null && resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Room> rooms = (List<Room>) resp.getData();
            this.allRooms = rooms;
            showMainTab();
        }
    }

    private void renderRooms(List<Room> rooms) {
        hotelsContainer.getChildren().clear();
        for (Room room : rooms) {
            String colorHex = room.getStatus().equals("FREE") ? "#e8f5e9" : "#f3e5f5";
            addHotelCard(room, colorHex);
        }
    }

    private void addHotelCard(Room room, String colorHex) {
        VBox card = new VBox();
        card.setPrefWidth(300);
        card.setPrefHeight(340);
        card.getStyleClass().add("hotel-card");

        VBox img = new VBox();
        img.setPrefHeight(160);
        img.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 8 0 0;");
        img.setAlignment(Pos.CENTER);
        img.getChildren().add(new Label(room.getType()));

        VBox body = new VBox(5);
        body.setPadding(new Insets(15));

        Label title = new Label("Pokój " + room.getNumber());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label price = new Label(room.getPrice() + " zł");
        price.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button viewBtn = new Button("Wybierz");
        viewBtn.getStyleClass().add("btn-card-action");
        viewBtn.setOnAction(e -> openDetails(room, colorHex));

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button starBtn = new Button("★");
        starBtn.getStyleClass().add("btn-star");
        if (favoriteRooms.contains(room))
            starBtn.getStyleClass().add("btn-star-active");

        starBtn.setOnAction(e -> {
            if (favoriteRooms.contains(room)) {
                favoriteRooms.remove(room);
                starBtn.getStyleClass().remove("btn-star-active");
            } else {
                favoriteRooms.add(room);
                starBtn.getStyleClass().add("btn-star-active");
            }
        });

        actions.getChildren().addAll(viewBtn, hSpacer, starBtn);
        body.getChildren().addAll(title, price, spacer, actions);
        card.getChildren().addAll(img, body);
        hotelsContainer.getChildren().add(card);
    }

    @FXML
    protected void showMainTab() {
        sectionTitle.setText("Dostępne obiekty:");
        renderRooms(allRooms);
        updateToolbar(btnMain);
    }

    @FXML
    protected void showFavsTab() {
        sectionTitle.setText("Twoje ulubione:");
        renderRooms(favoriteRooms);
        updateToolbar(btnFavs);
    }

    @FXML
    protected void showMyResTab() {
        sectionTitle.setText("Twoje rezerwacje:");
        renderRooms(myReservations);
        updateToolbar(btnMyRes);
    }

    private void updateToolbar(Button active) {
        btnMain.getStyleClass().remove("toolbar-btn-active");
        btnFavs.getStyleClass().remove("toolbar-btn-active");
        btnMyRes.getStyleClass().remove("toolbar-btn-active");
        active.getStyleClass().add("toolbar-btn-active");
    }

    private void openDetails(Room room, String colorHex) {
        detailTitle.setText("Pokój " + room.getNumber());
        detailPrice.setText(room.getPrice() + " zł / noc");
        detailImageBox.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 4;");
        detailRoomsContainer.getChildren().clear();
        addRoomVariant(room);
        listView.setVisible(false);
        detailsView.setVisible(true);
    }

    private void addRoomVariant(Room room) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 15;");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        Label typeLbl = new Label(room.getType());
        typeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        info.getChildren().addAll(typeLbl, new Label("Status: " + room.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button bookBtn = new Button("Rezerwuj");
        bookBtn.getStyleClass().add("btn-choose");
        bookBtn.setOnAction(e -> {
            if (!myReservations.contains(room))
                myReservations.add(room);
            openBookingModal(room);
        });

        row.getChildren().addAll(info, spacer, bookBtn);
        detailRoomsContainer.getChildren().add(row);
    }

    private void openBookingModal(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("booking-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 600);

            int fakeClientId = 1;

            BookingController controller = loader.getController();
            controller.setRoomData(room.getId(), room.getType(), room.getPrice(), fakeClientId);

            Stage stage = new Stage();
            stage.setTitle("Rezerwacja - " + room.getType());
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
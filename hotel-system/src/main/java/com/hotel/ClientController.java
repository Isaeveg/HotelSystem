package com.hotel;

import com.hotel.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientController {

    private static final Logger logger = LogManager.getLogger(ClientController.class);

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

    @FXML
    private TextField searchCityField;
    @FXML
    private DatePicker searchDateFrom;
    @FXML
    private DatePicker searchDateTo;

    private List<Room> allRooms = new ArrayList<>();
    private List<Room> favoriteRooms = new ArrayList<>();

    @FXML
    public void initialize() {

        loadFavorites();

        searchDateFrom.setValue(LocalDate.now());
        searchDateTo.setValue(LocalDate.now().plusDays(1));
        onSearchRooms();
    }

    private void loadFavorites() {
        int clientId = Session.getClientId();
        if (clientId == 0)
            return;

        Request req = new Request(RequestType.GET_FAVORITES, clientId);
        Response resp = NetworkClient.sendRequest(req);
        if (resp != null && resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Room> favs = (List<Room>) resp.getData();
            this.favoriteRooms = favs;
        }
    }

    @FXML
    protected void onResetSearch() {
        searchCityField.clear();
        searchDateFrom.setValue(LocalDate.now());
        searchDateTo.setValue(LocalDate.now().plusDays(1));

        sectionTitle.setText("Available hotels:");

        onSearchRooms();
    }

    @FXML
    protected void onSearchRooms() {
        String city = searchCityField.getText();
        LocalDate from = searchDateFrom.getValue();
        LocalDate to = searchDateTo.getValue();

        if (from == null || to == null || !to.isAfter(from)) {
            showAlert("Error", "Date 'To' must be after 'From'!");
            return;
        }

        if (city == null) {
            city = "";
        }

        Request req = new Request(RequestType.SEARCH_ROOMS, new Object[] { city, from.toString(), to.toString() });
        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Room> results = (List<Room>) resp.getData();

            this.allRooms = results;

            if (city.isEmpty()) {
                sectionTitle.setText("Search results (all cities): " + results.size());
            } else {
                sectionTitle.setText("Results for '" + city + "': " + results.size());
            }

            renderRooms(results);
        } else {
            showAlert("Info", "No rooms found or server error.");
        }
    }

    private void renderRooms(List<Room> rooms) {
        hotelsContainer.getChildren().clear();
        if (rooms.isEmpty()) {
            hotelsContainer.getChildren().add(new Label("No rooms available."));
            return;
        }
        for (Room room : rooms) {
            String colorHex = room.getStatus().equals("FREE") ? "#e8f5e9" : "#ffebee";
            addHotelCard(room, colorHex);
        }
    }

    private void addHotelCard(Room room, String colorHex) {
        VBox card = new VBox();
        card.setPrefWidth(320);
        card.setPrefHeight(400);
        card.getStyleClass().add("hotel-card");

        VBox img = new VBox();
        img.getStyleClass().add("card-image-area");

        boolean isFree = "FREE".equals(room.getStatus());
        img.getStyleClass().add(isFree ? "card-image-free" : "card-image-occupied");

        Label typeLabel = new Label(room.getType());
        typeLabel.getStyleClass().add("card-type-label");
        img.getChildren().add(typeLabel);

        VBox body = new VBox(8);
        body.setPadding(new Insets(15));
        VBox.setVgrow(body, Priority.ALWAYS);

        Label hotelName = new Label(room.getHotelName());
        hotelName.setWrapText(true);
        hotelName.getStyleClass().add("card-title");

        Label roomNr = new Label("Room no. " + room.getNumber());
        roomNr.getStyleClass().add("card-room-number");

        String descText = room.getDescription() != null ? room.getDescription() : "No description.";
        if (descText.length() > 80)
            descText = descText.substring(0, 77) + "...";
        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.getStyleClass().add("card-description");

        Label price = new Label(room.getPrice() + " PLN / night");
        price.getStyleClass().add("card-price");

        Label statusLbl = new Label(isFree ? "Available" : "Occupied");
        statusLbl.getStyleClass().addAll("status-label", isFree ? "status-free" : "status-occupied");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button viewBtn = new Button("Details / Book");
        viewBtn.getStyleClass().add("btn-card-action");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(viewBtn, Priority.ALWAYS);
        viewBtn.setOnAction(e -> openDetailsDialog(room, colorHex));

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button starBtn = new Button("★");
        starBtn.getStyleClass().add("btn-star");

        boolean isFav = favoriteRooms.stream().anyMatch(r -> r.getId() == room.getId());
        if (isFav)
            starBtn.getStyleClass().add("btn-star-active");

        starBtn.setOnAction(e -> {
            boolean currentlyFav = favoriteRooms.stream().anyMatch(r -> r.getId() == room.getId());
            int clientId = Session.getClientId();

            if (currentlyFav) {
                // Remove from server
                Request req = new Request(RequestType.REMOVE_FAVORITE, new Object[] { clientId, room.getId() });
                Response resp = NetworkClient.sendRequest(req);
                if (resp != null && resp.isSuccess()) {
                    favoriteRooms.removeIf(r -> r.getId() == room.getId());
                    starBtn.getStyleClass().remove("btn-star-active");
                    // Refresh if separate tab is open
                    if (sectionTitle.getText().startsWith("Twoje ulubione")) {
                        showFavsTab();
                    }
                }
            } else {
                // Add to server
                Request req = new Request(RequestType.ADD_FAVORITE, new Object[] { clientId, room.getId() });
                Response resp = NetworkClient.sendRequest(req);
                if (resp != null && resp.isSuccess()) {
                    favoriteRooms.add(room);
                    starBtn.getStyleClass().add("btn-star-active");
                }
            }
        });

        actions.getChildren().addAll(viewBtn, hSpacer, starBtn);

        body.getChildren().addAll(hotelName, roomNr, statusLbl, desc, spacer, price, actions);
        card.getChildren().addAll(img, body);
        hotelsContainer.getChildren().add(card);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    protected void showMainTab() {
        sectionTitle.setText("Available hotels:");
        renderRooms(allRooms);
        updateToolbar(btnMain);
        listView.setVisible(true);
        detailsView.setVisible(false);
    }

    @FXML
    protected void showFavsTab() {
        sectionTitle.setText("Your favorites:");
        renderRooms(favoriteRooms);
        updateToolbar(btnFavs);
        listView.setVisible(true);
        detailsView.setVisible(false);
    }

    @FXML
    protected void showMyResTab() {
        sectionTitle.setText("Your reservations:");
        loadReservations();
        updateToolbar(btnMyRes);
        listView.setVisible(true);
        detailsView.setVisible(false);
    }

    private void loadReservations() {
        Request req = new Request(RequestType.GET_BOOKINGS, null);
        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Booking> allBookings = (List<Booking>) resp.getData();

            int myId = Session.getClientId();
            List<Booking> myBookings = allBookings.stream()
                    .filter(b -> b.getClientId() == myId)
                    .collect(Collectors.toList());

            renderBookings(myBookings);
        } else {
            hotelsContainer.getChildren().clear();
            hotelsContainer.getChildren().add(new Label("Failed to load reservations."));
        }
    }

    private void renderBookings(List<Booking> bookings) {
        hotelsContainer.getChildren().clear();

        if (bookings.isEmpty()) {
            hotelsContainer.getChildren().add(new Label("No reservations."));
            return;
        }

        for (Booking b : bookings) {
            VBox card = new VBox(10);
            card.setPrefWidth(300);
            card.getStyleClass().add("hotel-card");

            card.getStyleClass().add("booking-card");

            Label title = new Label(b.getRoomNumber());
            title.getStyleClass().add("card-title");

            Label dateLbl = new Label("From: " + b.getCheckInDate() + "\nTo: " + b.getCheckOutDate());

            Label priceLbl = new Label("Price: " + b.getTotalPrice() + " PLN");
            priceLbl.getStyleClass().add("card-price");

            Label statusLbl = new Label("Status: " + b.getStatus());
            boolean isConfirmed = "CONFIRMED".equals(b.getStatus()) || "PAID".equals(b.getStatus());
            statusLbl.getStyleClass().addAll("status-label", isConfirmed ? "status-free" : "status-occupied");

            card.getChildren().addAll(title, dateLbl, priceLbl, statusLbl);
            hotelsContainer.getChildren().add(card);
        }
    }

    private void updateToolbar(Button active) {
        btnMain.getStyleClass().remove("toolbar-btn-active");
        btnFavs.getStyleClass().remove("toolbar-btn-active");
        btnMyRes.getStyleClass().remove("toolbar-btn-active");
        active.getStyleClass().add("toolbar-btn-active");
    }

    private void openDetailsDialog(Room room, String colorHex) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Room details - " + room.getNumber());

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));

        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(500);

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox imgPlaceholder = new VBox();
        imgPlaceholder.setPrefSize(100, 100);

        imgPlaceholder.setStyle("-fx-background-color: " + colorHex + ";");
        imgPlaceholder.getStyleClass().add("dialog-img-placeholder");
        imgPlaceholder.setAlignment(Pos.CENTER);
        imgPlaceholder.getChildren().add(new Label(room.getType()));

        VBox titleBox = new VBox(5);
        Label hName = new Label(room.getHotelName());

        hName.getStyleClass().add("dialog-hotel-name");
        Label rNr = new Label("Room " + room.getNumber());

        rNr.getStyleClass().add("dialog-room-number");

        Label status = new Label("Status: " + room.getStatus());

        status.getStyleClass().add("dialog-status");

        titleBox.getChildren().addAll(hName, rNr, status);
        header.getChildren().addAll(imgPlaceholder, titleBox);

        VBox content = new VBox(10);
        Label descLbl = new Label("Description:");

        descLbl.getStyleClass().add("dialog-section-label");
        Label fullDesc = new Label(room.getDescription() != null ? room.getDescription() : "No detailed description.");
        fullDesc.setWrapText(true);

        Label priceLbl = new Label("Price per night:");

        priceLbl.getStyleClass().add("dialog-section-label");
        Label priceVal = new Label(room.getPrice() + " PLN");

        priceVal.getStyleClass().add("dialog-price-val");

        content.getChildren().addAll(descLbl, fullDesc, new Separator(), priceLbl, priceVal);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> dialog.close());
        closeBtn.setPrefWidth(150);

        closeBtn.getStyleClass().add("dialog-btn-close");

        Button bookBtn = new Button("Book Now");
        bookBtn.getStyleClass().add("btn-choose");
        bookBtn.setPrefWidth(150);

        bookBtn.getStyleClass().add("dialog-btn-book");
        bookBtn.setOnAction(e -> {
            openBookingModal(room);
            dialog.close();
        });

        actions.getChildren().addAll(closeBtn, bookBtn);

        root.getChildren().addAll(header, new Separator(), content, new Region(), actions);
        VBox.setVgrow(root.getChildren().get(3), Priority.ALWAYS);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openBookingModal(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("booking-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 600);

            int realClientId = Session.getClientId();

            if (realClientId == 0) {
                logger.error("Error: Attempting to make a reservation without a customer ID!");
                return;
            }

            BookingController controller = loader.getController();
            controller.setRoomData(room.getId(), room.getType(), room.getPrice(), realClientId);

            Stage stage = new Stage();
            stage.setTitle("Booking - " + room.getType());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening booking modal: ", e);
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
            SceneManager.openModal("user-profile.fxml", "User Profile");
        } catch (IOException e) {
            logger.error("Error opening user profile: ", e);
        }
    }

    @FXML
    protected void onOpenFilters() {
        try {
            double maxInDb = allRooms.stream()
                    .mapToDouble(r -> Double.parseDouble(r.getPrice()))
                    .max().orElse(1000.0);
            FilterController controller = SceneManager.openModal("filter-view.fxml", "Filters",
                    (FilterController ctrl) -> {
                        ctrl.setMaxPriceLimit(maxInDb + 200.0);
                    });
            if (controller != null && controller.isApplied()) {
                applyFiltering(controller.getSelectedMaxPrice());
            }
        } catch (IOException e) {
            logger.error("Error opening filters: ", e);
        }
    }

    private void applyFiltering(double maxPrice) {
        List<Room> filtered = allRooms.stream()
                .filter(r -> Double.parseDouble(r.getPrice()) <= maxPrice)
                .collect(Collectors.toList());

        sectionTitle.setText("Filter results (up to " + (int) maxPrice + " PLN):");
        renderRooms(filtered);
    }
}
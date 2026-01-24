package com.hotel;

import com.hotel.common.*;
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
        // Load favorites FIRST
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

        sectionTitle.setText("Dostępne obiekty:");

        onSearchRooms();
    }

    @FXML
    protected void onSearchRooms() {
        String city = searchCityField.getText();
        LocalDate from = searchDateFrom.getValue();
        LocalDate to = searchDateTo.getValue();

        if (from == null || to == null || !to.isAfter(from)) {
            showAlert("Błąd", "Data 'Do' musi być późniejsza niż 'Od'!");
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
                sectionTitle.setText("Wyniki wyszukiwania (wszystkie miasta): " + results.size());
            } else {
                sectionTitle.setText("Wyniki dla '" + city + "': " + results.size());
            }

            renderRooms(results);
        } else {
            showAlert("Info", "Nie znaleziono pokoi lub błąd serwera.");
        }
    }

    private void renderRooms(List<Room> rooms) {
        hotelsContainer.getChildren().clear();
        if (rooms.isEmpty()) {
            hotelsContainer.getChildren().add(new Label("Brak dostępnych pokoi."));
            return;
        }
        for (Room room : rooms) {
            String colorHex = room.getStatus().equals("FREE") ? "#e8f5e9" : "#ffebee";
            addHotelCard(room, colorHex);
        }
    }

    private void addHotelCard(Room room, String colorHex) {
        VBox card = new VBox();
        card.setPrefWidth(320); // Slightly wider
        card.setPrefHeight(400); // Taller for description
        card.getStyleClass().add("hotel-card");

        // --- IMAGE AREA ---
        VBox img = new VBox();
        img.setPrefHeight(140);
        img.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 8 0 0;");
        img.setAlignment(Pos.CENTER);

        Label typeLabel = new Label(room.getType());
        typeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555;");
        img.getChildren().add(typeLabel);

        // --- BODY AREA ---
        VBox body = new VBox(8);
        body.setPadding(new Insets(15));
        VBox.setVgrow(body, Priority.ALWAYS);

        // Hotel Name
        Label hotelName = new Label(room.getHotelName());
        hotelName.setWrapText(true);
        hotelName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        // Room Number
        Label roomNr = new Label("Pokój nr " + room.getNumber());
        roomNr.setStyle("-fx-text-fill: #777; -fx-font-size: 12px;");

        // Description (Truncated)
        String descText = room.getDescription() != null ? room.getDescription() : "Brak opisu.";
        if (descText.length() > 80)
            descText = descText.substring(0, 77) + "...";
        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-style: italic;");
        desc.setPrefHeight(40); // Fixed height for consistency

        // Price
        Label price = new Label(room.getPrice() + " zł / noc");
        price.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        price.setStyle("-fx-text-fill: #003580;");

        // Status
        boolean isFree = "FREE".equals(room.getStatus());
        Label statusLbl = new Label(isFree ? "Dostępny" : "Zajęty");
        statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isFree ? "#2e7d32" : "#c62828") + ";");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // --- ACTIONS ---
        HBox actions = new HBox(10);
        Button viewBtn = new Button("Szczegóły / Rezerwuj");
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
        sectionTitle.setText("Dostępne obiekty:");
        renderRooms(allRooms);
        updateToolbar(btnMain);
        listView.setVisible(true);
        detailsView.setVisible(false);
    }

    @FXML
    protected void showFavsTab() {
        sectionTitle.setText("Twoje ulubione:");
        renderRooms(favoriteRooms);
        updateToolbar(btnFavs);
        listView.setVisible(true);
        detailsView.setVisible(false);
    }

    @FXML
    protected void showMyResTab() {
        sectionTitle.setText("Twoje rezerwacje:");
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
            hotelsContainer.getChildren().add(new Label("Nie udało się pobrać rezerwacji."));
        }
    }

    private void renderBookings(List<Booking> bookings) {
        hotelsContainer.getChildren().clear();

        if (bookings.isEmpty()) {
            hotelsContainer.getChildren().add(new Label("Brak rezerwacji."));
            return;
        }

        for (Booking b : bookings) {
            VBox card = new VBox(10);
            card.setPrefWidth(300);
            card.setStyle(
                    "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

            Label title = new Label(b.getRoomNumber()); // Contains hotel name + room nr
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label dateLbl = new Label("Od: " + b.getCheckInDate() + "\nDo: " + b.getCheckOutDate());
            Label priceLbl = new Label("Cena: " + b.getTotalPrice() + " PLN");
            Label statusLbl = new Label("Status: " + b.getStatus());
            if ("CONFIRMED".equals(b.getStatus()) || "PAID".equals(b.getStatus())) {
                statusLbl.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                statusLbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }

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
        dialog.setTitle("Szczegóły pokoju - " + room.getNumber());

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(500);

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox imgPlaceholder = new VBox();
        imgPlaceholder.setPrefSize(100, 100);
        imgPlaceholder.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8;");
        imgPlaceholder.setAlignment(Pos.CENTER);
        imgPlaceholder.getChildren().add(new Label(room.getType()));

        VBox titleBox = new VBox(5);
        Label hName = new Label(room.getHotelName());
        hName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label rNr = new Label("Pokój " + room.getNumber());
        rNr.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        Label status = new Label("Status: " + room.getStatus());
        status.setStyle("-fx-background-color: #eee; -fx-padding: 5 10; -fx-background-radius: 15;");

        titleBox.getChildren().addAll(hName, rNr, status);
        header.getChildren().addAll(imgPlaceholder, titleBox);

        // Details
        VBox content = new VBox(10);
        Label descLbl = new Label("Opis:");
        descLbl.setStyle("-fx-font-weight: bold;");
        Label fullDesc = new Label(room.getDescription() != null ? room.getDescription() : "Brak szczegółowego opisu.");
        fullDesc.setWrapText(true);

        Label priceLbl = new Label("Cena za dobę:");
        priceLbl.setStyle("-fx-font-weight: bold;");
        Label priceVal = new Label(room.getPrice() + " PLN");
        priceVal.setStyle("-fx-font-size: 24px; -fx-text-fill: #003580; -fx-font-weight: bold;");

        content.getChildren().addAll(descLbl, fullDesc, new Separator(), priceLbl, priceVal);

        // Actions
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button closeBtn = new Button("Zamknij");
        closeBtn.setOnAction(e -> dialog.close());
        closeBtn.setPrefWidth(150);
        closeBtn.setStyle("-fx-base: #eee; -fx-font-size: 14px; -fx-padding: 10 20;");

        Button bookBtn = new Button("Rezerwuj teraz");
        bookBtn.getStyleClass().add("btn-choose"); // Reuse existing style
        bookBtn.setPrefWidth(150);
        bookBtn.setStyle(
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #003580; -fx-text-fill: white;");
        bookBtn.setOnAction(e -> {
            openBookingModal(room);
            dialog.close();
        });

        actions.getChildren().addAll(closeBtn, bookBtn);

        root.getChildren().addAll(header, new Separator(), content, new Region(), actions);
        VBox.setVgrow(root.getChildren().get(3), Priority.ALWAYS); // Spacer

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
                System.err.println("Error: Attempting to make a reservation without a customer ID!");
                return;
            }

            BookingController controller = loader.getController();
            controller.setRoomData(room.getId(), room.getType(), room.getPrice(), realClientId);

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
            double maxInDb = allRooms.stream()
                    .mapToDouble(r -> Double.parseDouble(r.getPrice()))
                    .max().orElse(1000.0);
            FilterController controller = SceneManager.openModal("filter-view.fxml", "Filtry",
                    (FilterController ctrl) -> {
                        ctrl.setMaxPriceLimit(maxInDb + 200.0);
                    });
            if (controller != null && controller.isApplied()) {
                applyFiltering(controller.getSelectedMaxPrice());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyFiltering(double maxPrice) {
        List<Room> filtered = allRooms.stream()
                .filter(r -> Double.parseDouble(r.getPrice()) <= maxPrice)
                .collect(Collectors.toList());

        sectionTitle.setText("Wyniki filtrowania (do " + (int) maxPrice + " zł):");
        renderRooms(filtered);
    }
}
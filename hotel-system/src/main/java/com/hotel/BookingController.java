package com.hotel;

import com.hotel.common.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BookingController {

    @FXML
    private Label roomInfoLabel;
    @FXML
    private Label priceInfoLabel;
    @FXML
    private Label totalPriceLabel;

    @FXML
    private DatePicker dateFrom;
    @FXML
    private DatePicker dateTo;
    @FXML
    private Button confirmBtn;

    @FXML
    private VBox amenitiesContainer;

    private int currentRoomId;
    private int currentClientId;
    private BigDecimal roomPricePerNight;

    @FXML
    public void initialize() {
        dateFrom.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        dateTo.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());

        loadAmenities();
    }

    private void loadAmenities() {
        Response resp = NetworkClient.sendRequest(new Request(RequestType.GET_AMENITIES, null));
        if (resp != null && resp.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Amenity> loadedAmenities = (List<Amenity>) resp.getData();

            amenitiesContainer.getChildren().clear();
            for (Amenity a : loadedAmenities) {
                CheckBox cb = new CheckBox(a.getName() + " (" + a.getPrice() + " PLN)");
                cb.setUserData(a);
                cb.selectedProperty().addListener((obs, old, isSelected) -> calculateTotal());
                amenitiesContainer.getChildren().add(cb);
            }
        } else {
            amenitiesContainer.getChildren().add(new Label("Error loading amenities."));
        }
    }

    public void setRoomData(int roomId, String roomName, String price, int clientId) {
        this.currentRoomId = roomId;
        this.currentClientId = clientId;
        this.roomPricePerNight = new BigDecimal(price);

        roomInfoLabel.setText(roomName);
        priceInfoLabel.setText("Room price: " + price + " PLN / night");

        // Initial setup likely empty dates, so 0.00
        calculateTotal();
    }

    private BigDecimal calculateTotal() {
        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            totalPriceLabel.setText("0.00 PLN");
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(dateFrom.getValue(), dateTo.getValue());
        if (days < 1) {
            totalPriceLabel.setText("Invalid dates!");
            return BigDecimal.ZERO;
        }

        BigDecimal total = roomPricePerNight.multiply(new BigDecimal(days));

        for (Node node : amenitiesContainer.getChildren()) {
            if (node instanceof CheckBox cb && cb.isSelected()) {
                Amenity a = (Amenity) cb.getUserData();
                BigDecimal cost = new BigDecimal(a.getPrice());

                // Treat all amenities as fixed costs (one-time fee) to be consistent with Admin
                // panel logic.
                // If per-day calculation is needed in the future, database schema changes might
                // be required.

                total = total.add(cost);
            }
        }

        totalPriceLabel.setText(total.toString() + " PLN");
        return total;
    }

    @FXML
    protected void onConfirm() {
        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Fill in reservation dates!");
            return;
        }

        BigDecimal finalPrice = calculateTotal();
        if (finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            showAlert(Alert.AlertType.WARNING, "Error", "Check reservation dates!");
            return;
        }

        List<Integer> selectedAmenities = new ArrayList<>();
        for (Node node : amenitiesContainer.getChildren()) {
            if (node instanceof CheckBox cb && cb.isSelected()) {
                Amenity a = (Amenity) cb.getUserData();
                selectedAmenities.add(a.getId());
            }
        }

        Object[] requestData = {
                String.valueOf(currentClientId),
                String.valueOf(currentRoomId),
                dateFrom.getValue().toString(),
                dateTo.getValue().toString(),
                finalPrice.toString(),
                "PENDING", // Status
                selectedAmenities
        };

        Request req = new Request(RequestType.ADD_BOOKING, requestData);
        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Booking paid and confirmed!");
            closeModal();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to create booking.\n" + (resp != null ? resp.getMessage() : "No connection"));
        }
    }

    @FXML
    protected void onCancel() {
        closeModal();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeModal() {
        Stage stage = (Stage) confirmBtn.getScene().getWindow();
        stage.close();
    }
}
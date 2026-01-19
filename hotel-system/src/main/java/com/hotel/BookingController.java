package com.hotel;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import com.hotel.common.Room;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BookingController {

    @FXML private Label roomInfoLabel; 
    @FXML private Label priceInfoLabel; 
    @FXML private Label totalPriceLabel; 

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TextField phoneField;
    @FXML private Button confirmBtn;

    @FXML private CheckBox cbBreakfast;
    @FXML private CheckBox cbParking;
    @FXML private CheckBox cbSpa;

    private int currentRoomId;
    private int currentClientId;
    private BigDecimal roomPricePerNight;

    @FXML
    public void initialize() {
        dateFrom.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        dateTo.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        cbBreakfast.selectedProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        cbParking.selectedProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        cbSpa.selectedProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
    }

    public void setRoomData(int roomId, String roomName, String price, int clientId) {
        this.currentRoomId = roomId;
        this.currentClientId = clientId;
        this.roomPricePerNight = new BigDecimal(price);

        roomInfoLabel.setText(roomName);
        priceInfoLabel.setText("Cena pokoju: " + price + " PLN / noc");

        calculateTotal();
    }

    private BigDecimal calculateTotal() {
        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            totalPriceLabel.setText("0.00 PLN");
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(dateFrom.getValue(), dateTo.getValue());
        if (days < 1) {
            totalPriceLabel.setText("Błąd w datach!");
            return BigDecimal.ZERO;
        }

        BigDecimal total = roomPricePerNight.multiply(new BigDecimal(days));

        if (cbBreakfast.isSelected()) {
            total = total.add(new BigDecimal("40").multiply(new BigDecimal(days)));
        }

        if (cbParking.isSelected()) {
            total = total.add(new BigDecimal("25").multiply(new BigDecimal(days)));
        }

        if (cbSpa.isSelected()) {
            total = total.add(new BigDecimal("100"));
        }

        totalPriceLabel.setText(total.toString() + " PLN");
        return total;
    }

    @FXML
    protected void onConfirmBooking() { 
        if (dateFrom.getValue() == null || dateTo.getValue() == null || phoneField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Błąd", "Uzupełnij daty i numer telefonu!");
            return;
        }

        BigDecimal finalPrice = calculateTotal();
        if (finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            showAlert(Alert.AlertType.WARNING, "Błąd", "Sprawdź daty rezerwacji!");
            return;
        }

        List<Integer> selectedAmenities = new ArrayList<>();
        if (cbBreakfast.isSelected()) selectedAmenities.add(1);
        if (cbParking.isSelected()) selectedAmenities.add(2);
        if (cbSpa.isSelected()) selectedAmenities.add(3);

        Object[] requestData = {
                String.valueOf(currentClientId),
                String.valueOf(currentRoomId),
                dateFrom.getValue().toString(),
                dateTo.getValue().toString(),
                finalPrice.toString(),
                selectedAmenities
        };

        Request req = new Request(RequestType.ADD_BOOKING, requestData);
        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Rezerwacja opłacona i potwierdzona!");
            closeModal();
        } else {
            showAlert(Alert.AlertType.ERROR, "Błąd",
                    "Nie udało się utworzyć rezerwacji.\n" + (resp != null ? resp.getMessage() : "Brak połączenia"));
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
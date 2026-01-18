package com.hotel;

import com.hotel.common.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;
import java.util.List;

public class AdminAddRoomController {

    @FXML
    private TextField roomNumberField;
    @FXML
    private ComboBox<String> roomTypeCombo;
    @FXML
    private TextField roomPriceField;
    @FXML
    private TextArea descriptionField;

    @FXML
    private Button saveBtn;

    @FXML
    private ComboBox<Hotel> hotelCombo;

    private Integer editingRoomId = null;

    @FXML
    public void initialize() {
        loadHotels();
        if (roomTypeCombo.getItems().isEmpty()) {
            roomTypeCombo.setItems(FXCollections.observableArrayList("Standard", "Double", "Suite", "Deluxe"));
        }
    }

    private void loadHotels() {
        Request req = new Request(RequestType.GET_HOTELS, null);
        Response resp = NetworkClient.sendRequest(req);
        if (resp != null && resp.isSuccess() && resp.getData() instanceof List) {
            List<Hotel> hotels = (List<Hotel>) resp.getData();
            hotelCombo.setItems(FXCollections.observableArrayList(hotels));
        }
    }

    public void setRoomData(com.hotel.common.Room room) {
        this.editingRoomId = room.getId();
        this.roomNumberField.setText(room.getNumber());
        this.roomTypeCombo.setValue(room.getType());
        this.roomPriceField.setText(room.getPrice());

        this.descriptionField.setText(room.getDescription());

        for (Hotel h : hotelCombo.getItems()) {
            if (h.getId() == room.getHotelId()) {
                hotelCombo.setValue(h);
                break;
            }
        }

        this.saveBtn.setText("Zapisz zmiany");
    }

    @FXML
    protected void onSave() {
        if (roomNumberField.getText().isEmpty() || hotelCombo.getValue() == null
                || roomPriceField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Wybierz hotel, numer i cenę!");
            alert.showAndWait();
            return;
        }

        Hotel selectedHotel = hotelCombo.getValue();

        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            description = "";
        }

        Request req;
        if (editingRoomId == null) {
            String[] data = {
                    String.valueOf(selectedHotel.getId()),
                    roomNumberField.getText(),
                    roomTypeCombo.getValue(),
                    roomPriceField.getText(),
                    description
            };
            req = new Request(RequestType.ADD_ROOM, data);
        } else {
            String[] data = {
                    String.valueOf(editingRoomId),
                    roomNumberField.getText(),
                    roomTypeCombo.getValue(),
                    roomPriceField.getText(),
                    description
            };
            req = new Request(RequestType.UPDATE_ROOM, data);
        }

        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Sukces: " + resp.getMessage());
            alert.showAndWait();
            closeModal();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Błąd: " + (resp != null ? resp.getMessage() : "Brak połączenia"));
            alert.showAndWait();
        }
    }

    @FXML
    protected void onCancel() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
}
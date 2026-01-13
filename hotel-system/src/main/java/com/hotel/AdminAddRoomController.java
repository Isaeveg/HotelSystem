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
    private CheckBox checkBreakfast, checkParking, checkWifi, checkAC;
    @FXML
    private Button saveBtn;

    @FXML
    private ComboBox<Hotel> hotelCombo;

    private Integer editingRoomId = null;

    @FXML
    public void initialize() {
        loadHotels();
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

        for (Hotel h : hotelCombo.getItems()) {
            if (h.getId() == room.getHotelId()) {
                hotelCombo.setValue(h);
                break;
            }
        }

        String desc = room.getDescription();
        if (desc != null) {
            checkBreakfast.setSelected(desc.contains("Śniadanie"));
            checkParking.setSelected(desc.contains("Parking"));
            checkWifi.setSelected(desc.contains("Wi-Fi"));
            checkAC.setSelected(desc.contains("Klimatyzacja"));
        }

        this.saveBtn.setText("Zapisz zmiany");
    }

    @FXML
    protected void onSave() {
        if (roomNumberField.getText().isEmpty() || hotelCombo.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Wybierz hotel, numer i cenę!");
            alert.showAndWait();
            return;
        }

        Hotel selectedHotel = hotelCombo.getValue();

        StringBuilder descriptionBuilder = new StringBuilder();

        if (checkWifi.isSelected()) {
            descriptionBuilder.append("Wi-Fi, ");
        }
        if (checkBreakfast.isSelected()) {
            descriptionBuilder.append("Śniadanie, ");
        }
        if (checkParking.isSelected()) {
            descriptionBuilder.append("Parking, ");
        }
        if (checkAC.isSelected()) {
            descriptionBuilder.append("AC, ");
        }

        String description = descriptionBuilder.toString();

        if (description.endsWith(", ")) {
            description = description.substring(0, description.length() - 2);
        }

        if (description.isEmpty()) {
            description = "Brak dodatkowych udogodnień";
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
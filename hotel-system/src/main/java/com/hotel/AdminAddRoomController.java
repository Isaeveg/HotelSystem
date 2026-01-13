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

    // Метод для заполнения формы данными существующей комнаты
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

        // Парсим описание, чтобы выставить галочки
        String desc = room.getDescription();
        if (desc != null) {
            checkBreakfast.setSelected(desc.contains("Завтрак"));
            checkParking.setSelected(desc.contains("Парковка"));
            checkWifi.setSelected(desc.contains("Wi-Fi"));
            checkAC.setSelected(desc.contains("Кондиционер"));
        }

        this.saveBtn.setText("Zapisz zmiany");
    }

    @FXML
    protected void onSave() {
        // Проверка
        if (roomNumberField.getText().isEmpty() || hotelCombo.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Выберите отель, номер и цену!");
            alert.showAndWait();
            return;
        }

        Hotel selectedHotel = hotelCombo.getValue();

        StringBuilder descriptionBuilder = new StringBuilder();

        // Проверяем каждый чекбокс. Если выбран — добавляем текст.
        if (checkWifi.isSelected()) {
            descriptionBuilder.append("WiFi, ");
        }
        if (checkBreakfast.isSelected()) {
            descriptionBuilder.append("Breakfast, ");
        }
        if (checkParking.isSelected()) {
            descriptionBuilder.append("Parking, ");
        }
        if (checkAC.isSelected()) {
            descriptionBuilder.append("AC, ");
        }

        String description = descriptionBuilder.toString();

        // Если хочешь убрать последнюю запятую (для красоты):
        if (description.endsWith(", ")) {
            description = description.substring(0, description.length() - 2);
        }

        // И если вообще ничего не выбрали, можно задать дефолт
        if (description.isEmpty()) {
            description = "No special amenities";
        }

        Request req;
        if (editingRoomId == null) {
            // РЕЖИМ ДОБАВЛЕНИЯ
            String[] data = {
                    String.valueOf(selectedHotel.getId()), // [0] - ID отеля
                    roomNumberField.getText(), // [1]
                    roomTypeCombo.getValue(), // [2]
                    roomPriceField.getText(), // [3]
                    description // [4]
            };
            req = new Request(RequestType.ADD_ROOM, data);
        } else {
            // РЕЖИМ РЕДАКТИРОВАНИЯ (Тут логика чуть другая, можно отель не менять)
            // ... (твой код обновления)
            // Но лучше тоже передавать ID отеля, если вдруг комнату перенесли в другой
            // филиал (маловероятно, но всё же)
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
            alert.setContentText("Успех: " + resp.getMessage());
            alert.showAndWait();
            closeModal();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Ошибка: " + (resp != null ? resp.getMessage() : "Нет связи"));
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
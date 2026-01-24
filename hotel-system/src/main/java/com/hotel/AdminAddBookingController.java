package com.hotel;

import com.hotel.common.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AdminAddBookingController {
    @FXML
    private ComboBox<Client> clientCombo;
    @FXML
    private ComboBox<Room> roomCombo;
    @FXML
    private DatePicker checkInDate;
    @FXML
    private DatePicker checkOutDate;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private TextField priceField;

    @FXML
    private VBox amenitiesContainer;

    private Booking currentBooking;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("PENDING", "CONFIRMED", "CANCELLED"));
        statusCombo.getSelectionModel().select("PENDING");

        priceField.setEditable(false);
        priceField.setDisable(true);

        loadData();
        setupListeners();
    }

    private void setupListeners() {
        roomCombo.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotalPrice());
        checkInDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotalPrice());
        checkOutDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotalPrice());
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        Response respClients = NetworkClient.sendRequest(new Request(RequestType.GET_CLIENTS, null));
        if (respClients.isSuccess()) {
            clientCombo.setItems(FXCollections.observableArrayList((List<Client>) respClients.getData()));
            clientCombo.setCellFactory(param -> new ListCell<Client>() {
                @Override
                protected void updateItem(Client item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null)
                        setText(null);
                    else
                        setText(item.getFirstName() + " " + item.getLastName() + " (" + item.getEmail() + ")");
                }
            });
            clientCombo.setButtonCell(clientCombo.getCellFactory().call(null));
        }

        Response respRooms = NetworkClient.sendRequest(new Request(RequestType.GET_ROOMS, null));
        if (respRooms.isSuccess()) {
            roomCombo.setItems(FXCollections.observableArrayList((List<Room>) respRooms.getData()));
            roomCombo.setCellFactory(param -> new ListCell<Room>() {
                @Override
                protected void updateItem(Room item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null)
                        setText(null);
                    else
                        setText(item.getNumber() + " - " + item.getType() + " (" + item.getPrice() + " PLN)");
                }
            });
            roomCombo.setButtonCell(roomCombo.getCellFactory().call(null));
        }

        Response respAmenities = NetworkClient.sendRequest(new Request(RequestType.GET_AMENITIES, null));
        if (respAmenities.isSuccess()) {
            List<Amenity> ams = (List<Amenity>) respAmenities.getData();
            amenitiesContainer.getChildren().clear();

            for (Amenity amenity : ams) {
                CheckBox cb = new CheckBox(amenity.getName() + " (+" + amenity.getPrice() + " PLN)");
                cb.setUserData(amenity);

                cb.selectedProperty().addListener((obs, wasSelected, isSelected) -> calculateTotalPrice());

                amenitiesContainer.getChildren().add(cb);
            }
        }
    }

    private void calculateTotalPrice() {
        Room room = roomCombo.getValue();
        LocalDate in = checkInDate.getValue();
        LocalDate out = checkOutDate.getValue();

        if (room != null && in != null && out != null) {
            long days = ChronoUnit.DAYS.between(in, out);
            if (days < 1) {
                priceField.setText("0.00");
                return;
            }

            double roomPricePerNight = 0.0;
            try {
                roomPricePerNight = Double.parseDouble(room.getPrice());
            } catch (NumberFormatException e) {
            }

            double amenitiesCost = 0.0;
            for (Node node : amenitiesContainer.getChildren()) {
                if (node instanceof CheckBox cb && cb.isSelected()) {
                    Amenity a = (Amenity) cb.getUserData();
                    amenitiesCost += a.getPrice();
                }
            }

            double total = (roomPricePerNight * days) + amenitiesCost;
            priceField.setText(String.format("%.2f", total).replace(",", "."));
        }
    }

    public void setBookingData(Booking booking) {
        this.currentBooking = booking;

        if (clientCombo.getItems() != null) {
            for (Client c : clientCombo.getItems()) {
                if (c.getId() == booking.getClientId()) {
                    clientCombo.setValue(c);
                    break;
                }
            }
        }
        if (roomCombo.getItems() != null) {
            for (Room r : roomCombo.getItems()) {
                if (r.getId() == booking.getRoomId()) {
                    roomCombo.setValue(r);
                    break;
                }
            }
        }
        if (booking.getCheckInDate() != null)
            checkInDate.setValue(LocalDate.parse(booking.getCheckInDate()));
        if (booking.getCheckOutDate() != null)
            checkOutDate.setValue(LocalDate.parse(booking.getCheckOutDate()));
        statusCombo.setValue(booking.getStatus());

        if (booking.getAmenityIds() != null) {
            for (Node node : amenitiesContainer.getChildren()) {
                if (node instanceof CheckBox cb) {
                    Amenity a = (Amenity) cb.getUserData();
                    if (booking.getAmenityIds().contains(a.getId())) {
                        cb.setSelected(true);
                    }
                }
            }
        }
    }

    @FXML
    private void onSave() {
        Client selectedClient = clientCombo.getValue();
        Room selectedRoom = roomCombo.getValue();
        LocalDate in = checkInDate.getValue();
        LocalDate out = checkOutDate.getValue();
        String price = priceField.getText();
        String status = statusCombo.getValue();

        List<Integer> amenityIds = new ArrayList<>();
        for (Node node : amenitiesContainer.getChildren()) {
            if (node instanceof CheckBox cb && cb.isSelected()) {
                Amenity a = (Amenity) cb.getUserData();
                amenityIds.add(a.getId());
            }
        }

        if (selectedClient == null || selectedRoom == null || in == null || out == null || price.isEmpty()) {
            showAlert("Fill in all fields!");
            return;
        }

        Object[] data = {
                String.valueOf(selectedClient.getId()),
                String.valueOf(selectedRoom.getId()),
                in.toString(),
                out.toString(),
                price,
                status,
                amenityIds
        };

        Request req;
        if (currentBooking == null) {
            req = new Request(RequestType.ADD_BOOKING, data);
        } else {
            Object[] updateData = {
                    String.valueOf(currentBooking.getId()),
                    String.valueOf(selectedClient.getId()),
                    String.valueOf(selectedRoom.getId()),
                    in.toString(),
                    out.toString(),
                    price,
                    status,
                    amenityIds
            };
            req = new Request(RequestType.UPDATE_BOOKING, updateData);
        }

        Response resp = NetworkClient.sendRequest(req);
        if (resp.isSuccess()) {
            ((Stage) priceField.getScene().getWindow()).close();
        } else {
            showAlert("Error: " + resp.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        ((Stage) priceField.getScene().getWindow()).close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
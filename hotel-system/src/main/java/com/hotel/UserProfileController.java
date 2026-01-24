package com.hotel;

import com.hotel.common.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller for viewing user profile details.
 */
public class UserProfileController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;

    /**
     * Initializes the controller by loading client data.
     */
    @FXML
    public void initialize() {
        loadClientData();
    }

    private void loadClientData() {
        int clientId = Session.getClientId();
        if (clientId == 0) {
            nameLabel.setText("Error: You are not logged in");
            return;
        }

        Request req = new Request(RequestType.GET_CLIENT_DETAILS, clientId);
        Response resp = NetworkClient.sendRequest(req);

        if (resp != null && resp.isSuccess()) {
            Client client = (Client) resp.getData();
            updateUI(client);
        } else {
            nameLabel.setText("Error fetching data");
        }
    }

    private void updateUI(Client client) {
        if (client == null)
            return;
        nameLabel.setText("Name : " + client.getFirstName() + " " + client.getLastName());
        emailLabel.setText("Email: " + client.getEmail());
        phoneLabel.setText("Phone: " + client.getPhone());
    }

    /**
     * Closes the profile window.
     *
     * @param event the action event
     */
    @FXML
    protected void onClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
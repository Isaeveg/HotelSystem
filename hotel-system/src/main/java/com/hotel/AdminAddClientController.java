package com.hotel;

import com.hotel.common.Client;
import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for adding or editing a client in the admin interface.
 */
public class AdminAddClientController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button saveBtn;

    private Integer editingClientId = null;

    /**
     * Sets the client data for editing.
     *
     * @param client the client to edit
     */
    public void setClientData(Client client) {
        this.editingClientId = client.getId();
        firstNameField.setText(client.getFirstName());
        lastNameField.setText(client.getLastName());
        emailField.setText(client.getEmail());
        phoneField.setText(client.getPhone());

        passwordField.setPromptText("Leave empty to keep unchanged");
        saveBtn.setText("Save changes");
    }

    @FXML
    protected void onSave() {
        if (firstNameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Error", "First Name and Email are required!");
            return;
        }

        String fName = firstNameField.getText();
        String lName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String pass = passwordField.getText();

        Request req;
        if (editingClientId == null) {
            if (pass.isEmpty()) {
                showAlert("Error", "Enter password for new user!");
                return;
            }
            String[] data = { fName, lName, email, phone, pass };
            req = new Request(RequestType.ADD_CLIENT, data);
        } else {
            String[] data = { String.valueOf(editingClientId), fName, lName, email, phone };
            req = new Request(RequestType.UPDATE_CLIENT, data);
        }

        Response resp = NetworkClient.sendRequest(req);
        if (resp.isSuccess()) {
            showAlert("Success", resp.getMessage());
            closeModal();
        } else {
            showAlert("Error", resp.getMessage());
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
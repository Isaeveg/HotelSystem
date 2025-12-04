package com.hotel;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class FilterController {

    @FXML private Button applyBtn;
    @FXML private Slider priceSlider;
    @FXML private TextField priceInput;
    
    @FXML private CheckBox cbBreakfast;
    @FXML private CheckBox cbParking;
    @FXML private CheckBox cbPool;
    @FXML private CheckBox cbWifi;
    @FXML private CheckBox cbGood;
    @FXML private CheckBox cbExcellent;

    @FXML
    public void initialize() {
        priceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!priceInput.isFocused()) {
                priceInput.setText(String.format("%.0f", newVal));
            }
        });

        priceInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                priceInput.setText(newVal.replaceAll("[^\\d]", ""));
            }
            try {
                if (!newVal.isEmpty()) {
                    double val = Double.parseDouble(newVal);
                    if (val > priceSlider.getMax()) val = priceSlider.getMax();
                    priceSlider.setValue(val);
                }
            } catch (NumberFormatException e) { }
        });
    }

    @FXML
    protected void onReset() {
        priceSlider.setValue(priceSlider.getMax());
        cbBreakfast.setSelected(false);
        cbParking.setSelected(false);
        cbPool.setSelected(false);
        cbWifi.setSelected(false);
        cbGood.setSelected(false);
        cbExcellent.setSelected(false);
    }

    @FXML
    protected void onApply() {
        System.out.println("Zastosowano filtry. Max cena: " + priceInput.getText());
        Stage stage = (Stage) applyBtn.getScene().getWindow();
        stage.close();
    }
}
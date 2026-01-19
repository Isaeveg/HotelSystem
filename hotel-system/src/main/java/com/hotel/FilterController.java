package com.hotel;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;

public class FilterController {

    @FXML private Button applyBtn;
    @FXML private Slider priceSlider;
    @FXML private TextField priceInput;
    
    @FXML private CheckBox cbBreakfast, cbParking, cbSpa, cbLateCheckOut, cbCrib;

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

    public void setMaxPriceLimit(double max) {
        priceSlider.setMax(max);
        priceSlider.setValue(max);
        priceInput.setText(String.format("%.0f", max));
    }

    @FXML
    protected void onClearFilters() {
    priceSlider.setValue(priceSlider.getMax());
    cbBreakfast.setSelected(false);
    cbParking.setSelected(false);
    cbSpa.setSelected(false);
    cbLateCheckOut.setSelected(false);
    cbCrib.setSelected(false);
}

    @FXML
    protected void onApplyFilters() {
        System.out.println("Zastosowano filtry. Max cena: " + priceInput.getText());
        Stage stage = (Stage) applyBtn.getScene().getWindow();
        stage.close();
    }
}
package com.hotel;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;

public class FilterController {

    @FXML private Slider priceSlider;
    @FXML private TextField priceInput;
    
    @FXML private CheckBox cbBreakfast, cbParking, cbPool, cbWifi;
    @FXML private CheckBox cbGood, cbExcellent;

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
        if (cbPool != null) cbPool.setSelected(false);
        if (cbWifi != null) cbWifi.setSelected(false);
        if (cbGood != null) cbGood.setSelected(false);
        if (cbExcellent != null) cbExcellent.setSelected(false);
    }

    @FXML
    protected void onApplyFilters() {
        System.out.println("Zastosowano filtry. Max cena: " + priceInput.getText());
        ((Stage) priceSlider.getScene().getWindow()).close();
    }
}
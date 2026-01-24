package com.hotel;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;

public class FilterController {

    @FXML
    private Button applyBtn;
    @FXML
    private Slider priceSlider;
    @FXML
    private TextField priceInput;

    @FXML
    private CheckBox cbBreakfast, cbParking, cbSpa, cbLateCheckOut, cbCrib;

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
                    if (val > priceSlider.getMax())
                        val = priceSlider.getMax();
                    priceSlider.setValue(val);
                }
            } catch (NumberFormatException e) {
            }
        });
    }

    public void setMaxPriceLimit(double max) {
        priceSlider.setMax(max);
        priceSlider.setValue(max);
        priceInput.setText(String.format("%.0f", max));
    }

    public Button getApplyBtn() {
        return applyBtn;
    }

    public double getSelectedMaxPrice() {
        return priceSlider.getValue();
    }

    public List<String> getSelectedAmenities() {
        List<String> selected = new ArrayList<>();
        if (cbBreakfast.isSelected())
            selected.add("Breakfast");
        if (cbParking.isSelected())
            selected.add("Parking");
        if (cbSpa.isSelected())
            selected.add("SPA");
        if (cbLateCheckOut.isSelected())
            selected.add("Late Check-out");
        if (cbCrib.isSelected())
            selected.add("Crib");
        return selected;
    }

    private boolean applied = false;

    public boolean isApplied() {
        return applied;
    }

    @FXML
    protected void onApplyFilters() {
        applied = true;
        Stage stage = (Stage) applyBtn.getScene().getWindow();
        stage.close();
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

}
package hospital.management.components.search;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AdvancedSearchController {

    @FXML private TextField patientIdField;
    @FXML private TextField doctorNameField;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button searchBtn;
    @FXML private Button resetBtn;

    public void initialize() {
        statusFilter.getItems().addAll("All", "Admitted", "Discharged", "Pending", "Cancelled");
        statusFilter.setValue("All");
        searchBtn.setOnAction(e -> performSearch());
        resetBtn.setOnAction(e -> reset());
    }

    private void performSearch() {
        System.out.println("Advanced search — patientId=" + patientIdField.getText()
                + " doctor=" + doctorNameField.getText()
                + " date=" + appointmentDatePicker.getValue()
                + " status=" + statusFilter.getValue());
    }

    private void reset() {
        patientIdField.clear();
        doctorNameField.clear();
        appointmentDatePicker.setValue(null);
        statusFilter.setValue("All");
    }
}
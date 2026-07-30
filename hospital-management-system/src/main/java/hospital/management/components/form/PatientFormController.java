package hospital.management.components.form;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class PatientFormController {

    /* Step 1 — Personal Info */
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;

    /* Step 2 — Contact */
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;

    /* Step 3 — Medical */
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextArea allergiesArea;
    @FXML private ComboBox<String> assignedDoctorCombo;

    /* Step indicators */
    @FXML private Label step1Indicator;
    @FXML private Label step2Indicator;
    @FXML private Label step3Indicator;

    /* Step containers */
    @FXML private VBox step1Pane;
    @FXML private VBox step2Pane;
    @FXML private VBox step3Pane;

    /* Validation */
    @FXML private Label validationMessage;

    /* Nav buttons */
    @FXML private Button backBtn;
    @FXML private Button nextBtn;
    @FXML private Button submitBtn;

    private int currentStep = 1;

    public void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other");
        bloodGroupCombo.getItems().addAll("A+", "A−", "B+", "B−", "AB+", "AB−", "O+", "O−");
        assignedDoctorCombo.getItems().addAll("Dr. Smith", "Dr. Johnson", "Dr. Williams", "Dr. Brown");

        backBtn.setVisible(false);
        backBtn.setManaged(false);
        submitBtn.setVisible(false);
        submitBtn.setManaged(false);

        showStep(1);
    }

    @FXML
    private void handleNext() {
        if (!validateCurrentStep()) return;
        if (currentStep < 3) {
            currentStep++;
            showStep(currentStep);
        }
    }

    @FXML
    private void handleBack() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
        }
    }

    @FXML
    private void handleSubmit() {
        System.out.println("Patient form submitted: "
                + firstNameField.getText() + " " + lastNameField.getText());
    }

    private void showStep(int step) {
        step1Pane.setVisible(step == 1); step1Pane.setManaged(step == 1);
        step2Pane.setVisible(step == 2); step2Pane.setManaged(step == 2);
        step3Pane.setVisible(step == 3); step3Pane.setManaged(step == 3);

        backBtn.setVisible(step > 1);    backBtn.setManaged(step > 1);
        nextBtn.setVisible(step < 3);    nextBtn.setManaged(step < 3);
        submitBtn.setVisible(step == 3); submitBtn.setManaged(step == 3);

        updateStepIndicators(step);
        validationMessage.setText("");
    }

    private void updateStepIndicators(int step) {
        setIndicatorStyle(step1Indicator, step == 1 ? "active" : (step > 1 ? "completed" : ""));
        setIndicatorStyle(step2Indicator, step == 2 ? "active" : (step > 2 ? "completed" : ""));
        setIndicatorStyle(step3Indicator, step == 3 ? "active" : "");
    }

    private void setIndicatorStyle(Label indicator, String style) {
        indicator.getStyleClass().removeAll("active", "completed");
        if (!style.isEmpty()) indicator.getStyleClass().add(style);
    }

    private boolean validateCurrentStep() {
        validationMessage.setText("");
        if (currentStep == 1) {
            if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
                validationMessage.setText("First and last name are required.");
                return false;
            }
        }
        if (currentStep == 2) {
            if (phoneField.getText().isBlank() || emailField.getText().isBlank()) {
                validationMessage.setText("Phone and email are required.");
                return false;
            }
        }
        return true;
    }
}
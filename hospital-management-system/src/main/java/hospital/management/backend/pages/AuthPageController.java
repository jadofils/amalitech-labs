package hospital.management.backend.pages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AuthPageController {

    /* Login */
    @FXML private TextField      loginEmail;
    @FXML private PasswordField  loginPassword;
    @FXML private Label          loginError;

    /* Register */
    @FXML private TextField      regFirstName;
    @FXML private TextField      regLastName;
    @FXML private TextField      regEmail;
    @FXML private PasswordField  regPassword;
    @FXML private PasswordField  regConfirmPassword;
    @FXML private ComboBox<String> regRole;
    @FXML private Label          regError;

    /* Reset */
    @FXML private TextField      resetEmail;
    @FXML private Label          resetMessage;

    public void initialize() {
        if (regRole     != null) regRole.getItems().addAll("Doctor", "Nurse", "Receptionist", "Administrator");
        if (loginError  != null) loginError.setText("");
        if (regError    != null) regError.setText("");
        if (resetMessage != null) resetMessage.setText("");
    }

    @FXML
    private void handleLogin() {
        if (loginEmail.getText().isBlank() || loginPassword.getText().isBlank()) {
            loginError.setText("Email and password are required.");
            return;
        }
        navigateToDashboard();
    }

    @FXML
    private void handleRegister() {
        if (regFirstName.getText().isBlank() || regLastName.getText().isBlank()
                || regEmail.getText().isBlank() || regPassword.getText().isBlank()) {
            regError.setText("All fields are required.");
            return;
        }
        if (!regPassword.getText().equals(regConfirmPassword.getText())) {
            regError.setText("Passwords do not match.");
            return;
        }
        navigateToDashboard();
    }

    @FXML
    private void handleResetPassword() {
        if (resetEmail.getText().isBlank()) {
            resetMessage.setText("Please enter your email address.");
            return;
        }
        resetMessage.setText("Reset link sent to " + resetEmail.getText());
        resetMessage.getStyleClass().add("text-success");
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/hospital/management/frontend/pages/dashboard.fxml")
            );
            Parent root = loader.load();
            Scene scene = loginEmail.getScene();
            Scene newScene = new Scene(root, scene.getWidth(), scene.getHeight());
            newScene.getStylesheets().add(
                getClass().getResource("/hospital/management/css/global.css").toExternalForm()
            );
            ((Stage) scene.getWindow()).setScene(newScene);
        } catch (Exception e) {
            System.err.println("Navigation to dashboard failed: " + e.getMessage());
        }
    }
}
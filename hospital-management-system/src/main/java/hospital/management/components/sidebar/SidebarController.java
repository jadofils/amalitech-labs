package hospital.management.components.sidebar;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SidebarController {

    @FXML private VBox sidebarRoot;
    @FXML private Button dashboardBtn;
    @FXML private Button patientsBtn;
    @FXML private Button appointmentsBtn;
    @FXML private Button billingBtn;

    public void setActiveItem(String item) {
        clearActive();
        switch (item) {
            case "dashboard"     -> dashboardBtn.getStyleClass().add("active-item");
            case "patients"      -> patientsBtn.getStyleClass().add("active-item");
            case "appointments"  -> appointmentsBtn.getStyleClass().add("active-item");
            case "billing"       -> billingBtn.getStyleClass().add("active-item");
        }
    }

    private void clearActive() {
        dashboardBtn.getStyleClass().remove("active-item");
        patientsBtn.getStyleClass().remove("active-item");
        appointmentsBtn.getStyleClass().remove("active-item");
        billingBtn.getStyleClass().remove("active-item");
    }

    @FXML private void handleDashboard()    { navigate("/hospital/management/frontend/pages/dashboard.fxml"); }
    @FXML private void handlePatients()     { navigate("/hospital/management/frontend/pages/patients-page.fxml"); }
    @FXML private void handleAppointments() { navigate("/hospital/management/frontend/pages/appointments-page.fxml"); }
    @FXML private void handleBilling()      { navigate("/hospital/management/frontend/pages/billing-page.fxml"); }
    @FXML private void handleLogout()       { navigate("/hospital/management/frontend/pages/auth-pages.fxml"); }

    private void navigate(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = sidebarRoot.getScene();
            Scene newScene = new Scene(root, scene.getWidth(), scene.getHeight());
            newScene.getStylesheets().add(
                getClass().getResource("/hospital/management/css/global.css").toExternalForm()
            );
            ((Stage) scene.getWindow()).setScene(newScene);
        } catch (Exception e) {
            System.err.println("Navigation failed for " + fxmlPath + ": " + e.getMessage());
        }
    }
}
package hospital.management.components.buttons;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

public class LoadingButtonController {

    @FXML private Button actionBtn;
    @FXML private ProgressIndicator spinner;

    private Runnable action;

    public void initialize() {
        spinner.setVisible(false);
        spinner.setManaged(false);
        actionBtn.setOnAction(e -> runWithSpinner());
    }

    public void setLabel(String label) { actionBtn.setText(label); }

    public void setAction(Runnable action) { this.action = action; }

    private void runWithSpinner() {
        actionBtn.setDisable(true);
        spinner.setVisible(true);
        spinner.setManaged(true);
        new Thread(() -> {
            try {
                if (action != null) action.run();
            } finally {
                javafx.application.Platform.runLater(() -> {
                    actionBtn.setDisable(false);
                    spinner.setVisible(false);
                    spinner.setManaged(false);
                });
            }
        }).start();
    }
}
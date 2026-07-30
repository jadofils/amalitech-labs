package hospital.management.components.toast;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class ToastController {

    @FXML private HBox toastContainer;
    @FXML private Label toastMessage;

    public void initialize() {
        toastContainer.setVisible(false);
        toastContainer.setManaged(false);
    }

    public void showToast(String message, String type) {
        toastMessage.setText(message);
        toastContainer.getStyleClass().removeAll("toast-success", "toast-error", "toast-warning");
        switch (type) {
            case "success" -> toastContainer.getStyleClass().add("toast-success");
            case "error"   -> toastContainer.getStyleClass().add("toast-error");
            case "warning" -> toastContainer.getStyleClass().add("toast-warning");
        }
        toastContainer.setVisible(true);
        toastContainer.setManaged(true);
        toastContainer.setOpacity(1.0);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(2.5)),
            new KeyFrame(Duration.seconds(3.5), new KeyValue(toastContainer.opacityProperty(), 0.0))
        );
        timeline.setOnFinished(e -> {
            toastContainer.setVisible(false);
            toastContainer.setManaged(false);
            toastContainer.setOpacity(1.0);
        });
        timeline.play();
    }
}
package hospital.management.components.modal;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ModalController {

    @FXML private StackPane modalOverlay;
    @FXML private VBox modalBox;
    @FXML private Label modalTitle;
    @FXML private Label modalBody;
    @FXML private Button confirmBtn;
    @FXML private Button cancelBtn;

    private Consumer<Boolean> resultHandler;

    public void initialize() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        confirmBtn.setOnAction(e -> close(true));
        cancelBtn.setOnAction(e -> close(false));
        modalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == modalOverlay) close(false);
        });
    }

    public void show(String title, String message, Consumer<Boolean> onResult) {
        modalTitle.setText(title);
        modalBody.setText(message);
        resultHandler = onResult;
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    public void setConfirmLabel(String label) { confirmBtn.setText(label); }
    public void setCancelLabel(String label)  { cancelBtn.setText(label); }

    @FXML
    private void handleClose() { close(false); }

    private void close(boolean confirmed) {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        if (resultHandler != null) resultHandler.accept(confirmed);
    }
}
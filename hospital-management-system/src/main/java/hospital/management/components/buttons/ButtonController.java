package hospital.management.components.buttons;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonController {

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button deleteBtn;

    public void initialize() {
        saveBtn.setOnAction(e -> System.out.println("Save clicked"));
        cancelBtn.setOnAction(e -> System.out.println("Cancel clicked"));
        deleteBtn.setOnAction(e -> System.out.println("Delete clicked"));
    }

    public void setSaveHandler(Runnable handler)   { saveBtn.setOnAction(e -> handler.run()); }
    public void setCancelHandler(Runnable handler) { cancelBtn.setOnAction(e -> handler.run()); }
    public void setDeleteHandler(Runnable handler) { deleteBtn.setOnAction(e -> handler.run()); }

    public void setSaveLabel(String label)   { saveBtn.setText(label); }
    public void setDeleteVisible(boolean v)  { deleteBtn.setVisible(v); deleteBtn.setManaged(v); }
}
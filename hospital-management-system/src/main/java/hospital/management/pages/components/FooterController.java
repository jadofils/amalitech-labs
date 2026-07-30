package hospital.management.pages.components;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class FooterController {
    @FXML private Hyperlink contactLink;

    public void initialize() {
        if (contactLink != null) {
            contactLink.setOnAction(e -> System.out.println("Contact Us clicked"));
        }
    }
}
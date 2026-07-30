package hospital.management.components.footer;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class FooterController {

    @FXML private Hyperlink privacyLink;
    @FXML private Hyperlink termsLink;
    @FXML private Hyperlink contactLink;

    public void initialize() {
        privacyLink.setOnAction(e -> System.out.println("Privacy Policy clicked"));
        termsLink.setOnAction(e -> System.out.println("Terms of Service clicked"));
        contactLink.setOnAction(e -> System.out.println("Contact Us clicked"));
    }
}
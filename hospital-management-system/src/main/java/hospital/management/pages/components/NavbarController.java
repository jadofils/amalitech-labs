package hospital.management.pages.components;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class NavbarController {
    @FXML private ImageView logo;

    public void initialize() {
        var resource = getClass().getResource("/hospital/management/images/logo.png");
        if (resource != null) {
            logo.setImage(new javafx.scene.image.Image(resource.toExternalForm()));
        }
    }
}
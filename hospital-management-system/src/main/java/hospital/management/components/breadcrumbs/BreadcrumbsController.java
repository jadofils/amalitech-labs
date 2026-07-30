package hospital.management.components.breadcrumbs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

public class BreadcrumbsController {

    @FXML private HBox breadcrumbBar;

    public void setPath(List<String> segments) {
        breadcrumbBar.getChildren().clear();
        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i);
            boolean isLast = i == segments.size() - 1;

            if (isLast) {
                Label current = new Label(segment);
                current.getStyleClass().add("breadcrumb-current");
                breadcrumbBar.getChildren().add(current);
            } else {
                Button link = new Button(segment);
                link.getStyleClass().add("breadcrumb-item");
                link.setOnAction(e -> System.out.println("Navigate to: " + segment));
                Label sep = new Label("›");
                sep.getStyleClass().add("breadcrumb-separator");
                breadcrumbBar.getChildren().addAll(link, sep);
            }
        }
    }
}
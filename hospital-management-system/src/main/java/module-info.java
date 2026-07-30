module net.amalitech.hospitalmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    /* Application root */
    opens hospital.management to javafx.fxml;

    /* Models — opened so PropertyValueFactory can reach getters via reflection */
    opens hospital.management.model to javafx.fxml, javafx.base;
    exports hospital.management.model;

    /* Reusable component controllers */
    opens hospital.management.components.navbar        to javafx.fxml;
    opens hospital.management.components.footer        to javafx.fxml;
    opens hospital.management.components.sidebar       to javafx.fxml;
    opens hospital.management.components.breadcrumbs   to javafx.fxml;
    opens hospital.management.components.buttons       to javafx.fxml;
    opens hospital.management.components.search        to javafx.fxml;
    opens hospital.management.components.table         to javafx.fxml;
    opens hospital.management.components.form          to javafx.fxml;
    opens hospital.management.components.toast         to javafx.fxml;
    opens hospital.management.components.modal         to javafx.fxml;
    opens hospital.management.components.calendar      to javafx.fxml;
    opens hospital.management.components.stats         to javafx.fxml;
    opens hospital.management.components.profile       to javafx.fxml;
    opens hospital.management.components.notification  to javafx.fxml;
    opens hospital.management.components.searchdropdown to javafx.fxml;

    /* Page controllers (backend/pages — includes HomeController, DashboardController, etc.) */
    opens hospital.management.backend.pages to javafx.fxml;

    /* Legacy page controllers (kept for backward compatibility) */
    opens hospital.management.pages            to javafx.fxml;
    opens hospital.management.pages.components to javafx.fxml;

    exports hospital.management;
}
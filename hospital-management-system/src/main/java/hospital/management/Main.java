package hospital.management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/hospital/management/fxml/test-page.fxml")
        );
        Scene scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(
            getClass().getResource("/hospital/management/css/global.css").toExternalForm()
        );
        stage.setTitle("Hospital Management System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
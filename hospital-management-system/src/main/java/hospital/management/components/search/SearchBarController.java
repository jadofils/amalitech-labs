package hospital.management.components.search;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class SearchBarController {

    @FXML private TextField searchField;
    @FXML private Button searchBtn;

    private Consumer<String> onSearch;

    public void initialize() {
        searchBtn.setOnAction(e -> fireSearch());
        searchField.setOnAction(e -> fireSearch());
    }

    public void setOnSearch(Consumer<String> handler) { this.onSearch = handler; }

    public void setPromptText(String text) { searchField.setPromptText(text); }

    private void fireSearch() {
        String query = searchField.getText().trim();
        if (onSearch != null) {
            onSearch.accept(query);
        } else {
            System.out.println("Search: " + query);
        }
    }
}
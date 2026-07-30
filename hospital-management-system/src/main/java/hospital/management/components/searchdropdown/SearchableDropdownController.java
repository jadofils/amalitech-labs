package hospital.management.components.searchdropdown;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Consumer;

public class SearchableDropdownController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> dropdown;

    private List<String> allItems;
    private Consumer<String> onSelectionChanged;

    public void initialize() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterItems(newVal));
        dropdown.setOnAction(e -> {
            if (onSelectionChanged != null && dropdown.getValue() != null) {
                onSelectionChanged.accept(dropdown.getValue());
            }
        });
    }

    public void setItems(List<String> items) {
        allItems = items;
        dropdown.setItems(FXCollections.observableArrayList(items));
    }

    public void setPromptText(String text) { searchField.setPromptText(text); }

    public void setOnSelectionChanged(Consumer<String> handler) { onSelectionChanged = handler; }

    public String getSelected() { return dropdown.getValue(); }

    private void filterItems(String query) {
        if (allItems == null) return;
        if (query == null || query.isEmpty()) {
            dropdown.setItems(FXCollections.observableArrayList(allItems));
        } else {
            String lq = query.toLowerCase();
            dropdown.setItems(FXCollections.observableArrayList(
                allItems.stream().filter(i -> i.toLowerCase().contains(lq)).toList()
            ));
        }
        dropdown.show();
    }
}
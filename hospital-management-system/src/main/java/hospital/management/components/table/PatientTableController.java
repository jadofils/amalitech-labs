package hospital.management.components.table;

import hospital.management.model.Patient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class PatientTableController {

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> idColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, Integer> ageColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private Pagination pagination;

    private ObservableList<Patient> allPatients;
    private FilteredList<Patient> filteredPatients;
    private static final int PAGE_SIZE = 10;

    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        allPatients = FXCollections.observableArrayList(
            new Patient("P001", "Alice Johnson", 30, "Admitted",   "Female", "555-0101", "alice@example.com"),
            new Patient("P002", "Bob Smith",     45, "Discharged", "Male",   "555-0102", "bob@example.com"),
            new Patient("P003", "Clara Davis",   28, "Pending",    "Female", "555-0103", "clara@example.com"),
            new Patient("P004", "Daniel Brown",  52, "Admitted",   "Male",   "555-0104", "daniel@example.com"),
            new Patient("P005", "Eva Martinez",  35, "Discharged", "Female", "555-0105", "eva@example.com"),
            new Patient("P006", "Frank Wilson",  61, "Pending",    "Male",   "555-0106", "frank@example.com"),
            new Patient("P007", "Grace Lee",     22, "Admitted",   "Female", "555-0107", "grace@example.com")
        );

        filteredPatients = new FilteredList<>(allPatients, p -> true);
        patientTable.setItems(filteredPatients);

        if (pagination != null) {
            pagination.setPageCount(Math.max(1, (int) Math.ceil((double) allPatients.size() / PAGE_SIZE)));
        }

        patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientTable.setPlaceholder(new Label("No patients found"));
    }

    public void filter(String query) {
        filteredPatients.setPredicate(p -> {
            if (query == null || query.isEmpty()) return true;
            String lq = query.toLowerCase();
            return p.getName().toLowerCase().contains(lq)
                || p.getId().toLowerCase().contains(lq)
                || p.getStatus().toLowerCase().contains(lq);
        });
    }

    public void setPatients(ObservableList<Patient> patients) {
        this.allPatients = patients;
        this.filteredPatients = new FilteredList<>(patients, p -> true);
        patientTable.setItems(filteredPatients);
    }
}
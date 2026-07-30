package hospital.management.components.calendar;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

public class CalendarController {

    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private Consumer<LocalDate> onDateSelected;

    public void initialize() {
        currentMonth = YearMonth.now();
        renderCalendar();
    }

    public void setOnDateSelected(Consumer<LocalDate> handler) { this.onDateSelected = handler; }

    @FXML
    private void handlePrev() { currentMonth = currentMonth.minusMonths(1); renderCalendar(); }

    @FXML
    private void handleNext() { currentMonth = currentMonth.plusMonths(1); renderCalendar(); }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();

        monthYearLabel.setText(
            currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            + " " + currentMonth.getYear()
        );

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label header = new Label(days[i]);
            header.getStyleClass().add("calendar-day-header");
            header.setAlignment(Pos.CENTER);
            header.setMinWidth(36);
            calendarGrid.add(header, i, 0);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startCol = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        int col = startCol;
        int row = 1;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            Button cell = new Button(String.valueOf(day));
            cell.getStyleClass().add("calendar-day-cell");
            if (date.equals(today)) cell.getStyleClass().add("today");
            if (date.equals(selectedDate)) cell.getStyleClass().add("selected");
            final LocalDate d = date;
            cell.setOnAction(e -> selectDate(d));
            calendarGrid.add(cell, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    private void selectDate(LocalDate date) {
        selectedDate = date;
        renderCalendar();
        if (onDateSelected != null) onDateSelected.accept(date);
    }
}
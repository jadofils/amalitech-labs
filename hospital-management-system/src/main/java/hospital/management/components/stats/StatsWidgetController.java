package hospital.management.components.stats;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StatsWidgetController {

    @FXML private Label totalPatientsValue;
    @FXML private Label todayAppointmentsValue;
    @FXML private Label revenueValue;
    @FXML private Label pendingBillsValue;

    @FXML private Label totalPatientsTrend;
    @FXML private Label todayAppointmentsTrend;
    @FXML private Label revenueTrend;
    @FXML private Label pendingBillsTrend;

    public void initialize() {
        setStats(1_284, 47, 38_500.0, 23);
    }

    public void setStats(int totalPatients, int todayAppts, double revenue, int pendingBills) {
        totalPatientsValue.setText(String.format("%,d", totalPatients));
        todayAppointmentsValue.setText(String.valueOf(todayAppts));
        revenueValue.setText(String.format("$%,.0f", revenue));
        pendingBillsValue.setText(String.valueOf(pendingBills));

        totalPatientsTrend.setText("+12 this week");
        totalPatientsTrend.getStyleClass().add("trend-up");
        todayAppointmentsTrend.setText("+5 vs yesterday");
        todayAppointmentsTrend.getStyleClass().add("trend-up");
        revenueTrend.setText("+8% this month");
        revenueTrend.getStyleClass().add("trend-up");
        pendingBillsTrend.setText("-3 resolved today");
        pendingBillsTrend.getStyleClass().add("trend-down");
    }
}
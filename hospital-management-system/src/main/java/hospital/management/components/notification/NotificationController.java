package hospital.management.components.notification;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class NotificationController {

    @FXML private VBox notificationList;
    @FXML private Label unreadCount;

    public record Notification(String title, String body, String time, boolean unread, boolean critical) {}

    public void initialize() {
        loadSampleNotifications();
    }

    private void loadSampleNotifications() {
        List<Notification> notifications = List.of(
            new Notification("Lab Results Ready",  "Alice Johnson's blood test results are available.", "5 min ago",  true,  false),
            new Notification("Emergency Alert",    "Patient Bob Smith requires immediate attention.",   "12 min ago", true,  true),
            new Notification("Appointment Booked", "New appointment scheduled for Dr. Williams.",       "1 hr ago",   false, false),
            new Notification("Payment Received",   "Invoice #INV-0045 has been paid.",                 "3 hr ago",   false, false)
        );
        long unread = notifications.stream().filter(Notification::unread).count();
        unreadCount.setText(String.valueOf(unread));
        unreadCount.setVisible(unread > 0);

        for (Notification n : notifications) {
            notificationList.getChildren().add(buildItem(n));
        }
    }

    private VBox buildItem(Notification n) {
        VBox item = new VBox(3);
        item.getStyleClass().add("notification-item");
        if (n.unread())    item.getStyleClass().add("unread");
        if (n.critical())  item.getStyleClass().add("notification-critical");

        Label title = new Label(n.title());
        title.getStyleClass().add("notification-title");
        Label body = new Label(n.body());
        body.getStyleClass().add("notification-body");
        Label time = new Label(n.time());
        time.getStyleClass().add("notification-time");

        item.getChildren().addAll(title, body, time);
        return item;
    }

    @FXML
    private void handleViewAll() {
        System.out.println("View all notifications");
    }
}
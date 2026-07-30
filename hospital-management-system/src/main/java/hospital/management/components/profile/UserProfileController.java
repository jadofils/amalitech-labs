package hospital.management.components.profile;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserProfileController {

    @FXML private Label avatarInitials;
    @FXML private Label profileName;
    @FXML private Label profileRole;
    @FXML private Label roleBadge;

    public void initialize() {
        setProfile("Dr. Admin User", "Administrator", "ADMIN");
    }

    public void setProfile(String name, String role, String roleCode) {
        profileName.setText(name);
        profileRole.setText(role);
        roleBadge.setText(roleCode);
        String[] parts = name.split(" ");
        String initials = parts.length >= 2
            ? String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)
            : name.substring(0, Math.min(2, name.length())).toUpperCase();
        avatarInitials.setText(initials);
    }

    @FXML private void handleEditProfile() { System.out.println("Edit profile"); }
    @FXML private void handleSettings()    { System.out.println("Settings"); }
    @FXML private void handleLogout()      { System.out.println("Logout"); }
}
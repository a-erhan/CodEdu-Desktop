package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Role;
import com.codedu.models.User;
import com.codedu.models.UserInventory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Login screen.
 * Validates credentials and transitions to MainShell on success.
 */
@Controller
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        // Apply Nord typography to the login title
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        // Make login button a primary, rounded CTA
        if (loginButton != null) {
            loginButton.getStyleClass().addAll(Styles.ACCENT, Styles.LARGE, Styles.ROUNDED);
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in both email and password.");
            return;
        }

        // Create user and transition to main shell
        User user = new User();
        user.setEmail(email);
        user.setUsername(email.contains("@") ? email.substring(0, email.indexOf('@')) : email);
        user.setPassword(password);
        user.setRole(Role.STUDENT);
        user.setTokenBalance(500);
        user.setInventory(UserInventory.builder().build());

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/MainShell.fxml"));
            Parent root = loader.load();

            // Pass user to main shell controller
            MainShellController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) emailField.getScene().getWindow();
            double w = Math.max(800, stage.getWidth());
            double h = Math.max(600, stage.getHeight());
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            errorLabel.setText("Failed to load application.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Failed to open registration.");
            e.printStackTrace();
        }
    }
}

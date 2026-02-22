package com.codedu.controllers;

import com.codedu.models.User;
import com.codedu.models.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Login screen.
 * Validates credentials and transitions to MainShell on success.
 */
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

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

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/MainShell.fxml"));
            Parent root = loader.load();

            // Pass user to main shell controller
            MainShellController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(
                    getClass().getResource("/com/codedu/views/application.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Failed to load application.");
            e.printStackTrace();
        }
    }
}

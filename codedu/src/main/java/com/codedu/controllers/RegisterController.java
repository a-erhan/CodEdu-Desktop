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
 * Simple registration screen that creates a User and opens the main shell.
 * In a real app, this would persist the user via a service/repository.
 */
@Controller
public class RegisterController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button registerButton;

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (registerButton != null) {
            registerButton.getStyleClass().addAll(Styles.ACCENT, Styles.LARGE, Styles.ROUNDED);
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // Mock user creation (no persistence yet)
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.STUDENT);
        user.setTokenBalance(500);
        user.setInventory(UserInventory.builder().build());

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/MainShell.fxml"));
            Parent root = loader.load();

            MainShellController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Failed to create account.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Failed to go back to login.");
            e.printStackTrace();
        }
    }
}


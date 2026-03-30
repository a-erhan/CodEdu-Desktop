package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
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
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button registerButton;
    @Autowired
    private AuthService authService;
    @Autowired
    private org.springframework.context.ApplicationContext context;

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
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // Derive username from email (e.g., "john.doe" from "john.doe@example.com")
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

        // Attempt to register the user via AuthService
        String registrationResult = authService.register(username, email, password);

        // If registration fails (e.g., email already exists), show the error and stop
        if (!"SUCCESS".equals(registrationResult)) {
            errorLabel.setText(registrationResult);
            return;
        }

        // Registration successful! Now let's automatically log them in to fetch the real User entity from DB
        User loggedInUser = authService.login(email, password);
        if (loggedInUser == null) {
            errorLabel.setText("Account created, but automatic login failed. Please go to Login page.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/MainShell.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            MainShellController controller = loader.getController();
            controller.setUser(loggedInUser);

            Stage stage = (Stage) emailField.getScene().getWindow();
            double w = Math.max(800, stage.getWidth());
            double h = Math.max(600, stage.getHeight());
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
            stage.setMaximized(true);
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
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Failed to go back to login.");
            e.printStackTrace();
        }
    }
}


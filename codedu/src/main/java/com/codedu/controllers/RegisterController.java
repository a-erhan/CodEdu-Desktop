package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.AuthService;
import com.codedu.ui.StageNavigator;
import javafx.fxml.FXML;
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
    private StageNavigator navigator;

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

        String registrationResult = authService.register(email, password);

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
            Stage stage = (Stage) emailField.getScene().getWindow();
            navigator.replaceScene(stage, "/com/codedu/views/MainShell.fxml", MainShellController.class,
                    c -> c.setUser(loggedInUser));
            stage.setMaximized(true);
        } catch (Exception e) {
            errorLabel.setText("Failed to create account.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            navigator.replaceSceneFixed(stage, "/com/codedu/views/Login.fxml", LoginController.class, null,
                    1200, 750);
        } catch (Exception e) {
            errorLabel.setText("Failed to go back to login.");
            e.printStackTrace();
        }
    }
}


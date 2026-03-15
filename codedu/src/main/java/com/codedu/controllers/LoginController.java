package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Role;
import com.codedu.models.User;
import com.codedu.models.UserInventory;
import com.codedu.models.UserGameState;
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
import com.codedu.services.AuthService;
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
    @Autowired
    private AuthService authService;
    @Autowired
    private org.springframework.context.ApplicationContext context;


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
            errorLabel.setText("fill in both email and password.");
            return;
        }

        User loggedInUser = authService.login(email, password);
        if (loggedInUser == null) {
            errorLabel.setText("Invalid email or password. Please try again or create an account.");
            return;
        }


        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/MainShell.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            // Pass user to main shell controller
            MainShellController controller = loader.getController();
            controller.setUser(loggedInUser);

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
            loader.setControllerFactory(context::getBean);
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

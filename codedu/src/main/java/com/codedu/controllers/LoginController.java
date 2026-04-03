package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.user.UserDTO;
import com.codedu.dtos.user.UserLoginDTO;
import com.codedu.models.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.codedu.services.interfaces.AuthService;
import com.codedu.services.interfaces.UserService;
import com.codedu.ui.StageNavigator;
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
    private UserService userService;
    @Autowired
    private StageNavigator navigator;


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

        UserDTO loggedInUser = authService.login(new UserLoginDTO(email, password));
        if (loggedInUser == null) {
            errorLabel.setText("Invalid email or password. Please try again or create an account.");
            return;
        }

        try {
            // Fetch the full User entity for MainShellController
            User fullUser = userService.getUserWithProfileData(loggedInUser.username()).orElse(null);
            if (fullUser == null) {
                errorLabel.setText("Failed to load user profile.");
                return;
            }
            Stage stage = (Stage) emailField.getScene().getWindow();
            navigator.replaceScene(stage, "/com/codedu/views/MainShell.fxml", MainShellController.class,
                    c -> c.setUser(fullUser));
            stage.setMaximized(true);
        } catch (Exception e) {
            errorLabel.setText("Failed to load application.");
            e.printStackTrace();
        }
    }
    @FXML
    private void handleOpenRegister() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            navigator.replaceSceneFixed(stage, "/com/codedu/views/Register.fxml", RegisterController.class, null,
                    1200, 750);
        } catch (Exception e) {
            errorLabel.setText("Failed to open registration.");
            e.printStackTrace();
        }
    }
}

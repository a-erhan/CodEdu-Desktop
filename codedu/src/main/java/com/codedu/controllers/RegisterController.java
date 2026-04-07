package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.user.UserDTO;
import com.codedu.dtos.user.UserLoginDTO;
import com.codedu.dtos.user.UserRegisterDTO;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.AuthService;
import com.codedu.services.interfaces.UserService;
import com.codedu.ui.StageNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RegisterController {

    @FXML private Label titleLabel, errorLabel;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Button registerButton, backButton;
    @FXML private VBox registerCard;

    @Autowired private AuthService authService;
    @Autowired private UserService userService;
    @Autowired private StageNavigator navigator;

    @FXML
    public void initialize() {
        String LOGO_BLUE = "#00AEEF";
        String LOGO_ORANGE = "#F7941D";

        if (titleLabel != null) {
            titleLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold;");
        }

        if (registerButton != null) {
            registerButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: " + LOGO_BLUE + "; " +
                            "-fx-border-width: 1.5; " +
                            "-fx-border-radius: 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold;"
            );
        }

        if (errorLabel != null) {
            errorLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + ";");
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

        String registrationResult = authService.register(new UserRegisterDTO(null, email, password));

        if (!"SUCCESS".equals(registrationResult)) {
            errorLabel.setText(registrationResult);
            return;
        }

        UserDTO loggedInUser = authService.login(new UserLoginDTO(email, password));
        if (loggedInUser == null) {
            errorLabel.setText("Account created! Please go to Login page to sign in.");
            return;
        }

        try {
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
            errorLabel.setText("An error occurred during setup.");
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
            errorLabel.setText("Failed to return to login.");
            e.printStackTrace();
        }
    }
}
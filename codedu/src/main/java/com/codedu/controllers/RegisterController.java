package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.user.LoginResult;
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

    @FXML
    private Label titleLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backButton;
    @FXML
    private VBox registerCard;

    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Autowired
    private StageNavigator navigator;

    @FXML
    public void initialize() {
        String logoBlue = "#00AEEF";
        String logoOrange = "#F7941D";

        if (titleLabel != null) {
            titleLabel.setStyle("-fx-text-fill: " + logoOrange + "; -fx-font-weight: bold;");
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }

        if (registerButton != null) {
            registerButton.setStyle(
                    "-fx-background-color: transparent; "
                            + "-fx-text-fill: white; "
                            + "-fx-border-color: " + logoBlue + "; "
                            + "-fx-border-width: 1.5; "
                            + "-fx-border-radius: 5; "
                            + "-fx-cursor: hand; "
                            + "-fx-font-weight: bold;");
        }

        if (errorLabel != null) {
            errorLabel.setStyle("-fx-text-fill: " + logoOrange + ";");
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

        errorLabel.setStyle("");

        String registrationResult;
        try {
            registrationResult = authService.register(new UserRegisterDTO(null, email, password));
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            errorLabel.setText("Could not complete registration: " + root.getMessage());
            return;
        }

        if (!registrationResult.startsWith("SUCCESS")) {
            errorLabel.setText(registrationResult);
            return;
        }

        if ("SUCCESS_SKIP_EMAIL".equals(registrationResult)) {
            LoginResult loginResult = authService.login(new UserLoginDTO(email, password));
            if (!loginResult.success()) {
                errorLabel.setText(loginResult.message() != null ? loginResult.message()
                        : "Account created, but sign-in failed. Use the Login page.");
                return;
            }
            UserDTO loggedInUser = loginResult.user();
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
            return;
        }

        errorLabel.setStyle("-fx-text-fill: #a3be8c;");
        errorLabel.setText(
                "Account created. We've sent a verification link to your email. "
                        + "Open the link to verify, then sign in.");
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

package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.user.LoginResult;
import com.codedu.dtos.user.UserDTO;
import com.codedu.dtos.user.UserLoginDTO;
import com.codedu.models.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.codedu.services.interfaces.AuthService;
import com.codedu.services.interfaces.UserService;
import com.codedu.ui.StageNavigator;

@Controller
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Hyperlink resendVerificationLink;
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
    private VBox loginCard;

    @FXML
    public void initialize() {
        String LOGO_BLUE = "#00AEEF";
        String LOGO_ORANGE = "#F7941D";
        String CARD_BG = "#242B33";
        String BORDER_BLUE = "#00AEEF";

        loginCard.setStyle(
                "-fx-background-color: " + CARD_BG + "; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: " + BORDER_BLUE + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 20, 0, 0, 10);"
        );

        loginButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: " + LOGO_BLUE + "; " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        titleLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold;");
        titleLabel.getStyleClass().add(Styles.TITLE_3);
        if (resendVerificationLink != null) {
            resendVerificationLink.setVisited(false);
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("fill in both email and password.");
            return;
        }

        errorLabel.setStyle("");
        LoginResult loginResult = authService.login(new UserLoginDTO(email, password));
        if (!loginResult.success()) {
            errorLabel.setText(loginResult.message() != null ? loginResult.message()
                    : "Invalid email or password. Please try again or create an account.");
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
            errorLabel.setText("Failed to load application.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResendVerification() {
        String email = emailField.getText().trim();
        errorLabel.setStyle("");
        if (email.isEmpty()) {
            errorLabel.setText("Enter your email above, then click resend.");
            return;
        }
        try {
            String result = authService.resendVerificationEmail(email);
            if ("SUCCESS".equals(result)) {
                errorLabel.setStyle("-fx-text-fill: #a3be8c;");
                errorLabel.setText("Verification email sent. Check your inbox.");
            } else {
                errorLabel.setText(result.startsWith("ERROR: ") ? result.substring("ERROR: ".length()) : result);
            }
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            errorLabel.setText("Could not send email: " + root.getMessage());
        }
        if (resendVerificationLink != null) {
            resendVerificationLink.setVisited(false);
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

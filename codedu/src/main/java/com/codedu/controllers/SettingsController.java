package com.codedu.controllers;

import com.codedu.models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Settings view.
 * Handles theme switching, notifications, language, password change, and
 * account removal.
 */
@Controller
public class SettingsController {

    @FXML
    private Button btnThemeToggle;
    @FXML
    private CheckBox chkPushNotif;
    @FXML
    private CheckBox chkEmailNotif;
    @FXML
    private CheckBox chkDailyReminder;
    @FXML
    private ComboBox<String> comboLanguage;
    @FXML
    private Button btnChangePassword;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnRemoveAccount;

    private User user;
    private boolean darkMode = false;
    private Runnable themeToggleCallback;

    public void setUserModel(User user) {
        this.user = user;
        updateThemeButton();
    }

    /**
     * Callback invoked when the user toggles dark/light mode,
     * so the shell can swap stylesheets.
     */
    public void setThemeToggleCallback(Runnable callback) {
        this.themeToggleCallback = callback;
    }

    @FXML
    public void initialize() {
        comboLanguage.setItems(FXCollections.observableArrayList(
                "English", "Türkçe", "Deutsch", "Français", "Español"));
        comboLanguage.getSelectionModel().selectFirst();

        btnThemeToggle.setOnAction(e -> {
            darkMode = !darkMode;
            updateThemeButton();
            if (themeToggleCallback != null)
                themeToggleCallback.run();
        });

        btnChangePassword.setOnAction(e -> showChangePasswordDialog());
        btnLogout.setOnAction(e -> showLogoutDialog());
        btnRemoveAccount.setOnAction(e -> showRemoveAccountDialog());
    }

    private void updateThemeButton() {
        btnThemeToggle.setText(darkMode ? "Dark mode" : "Light mode");
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    private void showChangePasswordDialog() {
        Stage dialog = createDialog("Change Password", 380, 320);

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));

        Label title = new Label("Change password");

        PasswordField oldPwd = new PasswordField();
        oldPwd.setPromptText("Current Password");
        styleTextField(oldPwd);

        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("New Password");
        styleTextField(newPwd);

        PasswordField confirmPwd = new PasswordField();
        confirmPwd.setPromptText("Confirm New Password");
        styleTextField(confirmPwd);

        Label feedback = new Label();

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Password");
        saveBtn.setOnAction(e -> {
            if (newPwd.getText().isEmpty() || confirmPwd.getText().isEmpty()) {
                feedback.setText("Please fill in all fields.");
            } else if (!newPwd.getText().equals(confirmPwd.getText())) {
                feedback.setText("New passwords do not match.");
            } else if (newPwd.getText().length() < 6) {
                feedback.setText("Password must be at least 6 characters.");
            } else {
                feedback.setText("✓ Password changed successfully!");
                saveBtn.setDisable(true);
            }
        });

        btnRow.getChildren().addAll(cancelBtn, saveBtn);
        content.getChildren().addAll(title, oldPwd, newPwd, confirmPwd, feedback, btnRow);

        dialog.setScene(new Scene(content));
        dialog.showAndWait();
    }

    private void showLogoutDialog() {
        Stage dialog = createDialog("Log Out", 360, 200);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));

        Label title = new Label("Log Out?");

        Label msg = new Label("Are you sure you want to log out?");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("Log Out");
        confirmBtn.setOnAction(e -> dialog.close());

        btnRow.getChildren().addAll(cancelBtn, confirmBtn);
        content.getChildren().addAll(title, msg, btnRow);

        dialog.setScene(new Scene(content));
        dialog.showAndWait();
    }

    private void showRemoveAccountDialog() {
        Stage dialog = createDialog("Remove Account", 400, 240);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));

        Label title = new Label("Remove account");

        Label msg = new Label(
                "This action is permanent and cannot be undone.\nAll your progress, tokens, and purchases will be lost.");
        msg.setWrapText(true);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button deleteBtn = new Button("Delete My Account");
        deleteBtn.setOnAction(e -> dialog.close());

        btnRow.getChildren().addAll(cancelBtn, deleteBtn);
        content.getChildren().addAll(title, msg, btnRow);

        dialog.setScene(new Scene(content));
        dialog.showAndWait();
    }

    private Stage createDialog(String title, int width, int height) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(title);
        dialog.setWidth(width);
        dialog.setHeight(height);
        dialog.setResizable(false);
        return dialog;
    }

    private void styleTextField(TextField field) {
        field.setMaxWidth(280);
    }
}

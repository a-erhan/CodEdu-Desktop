package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.User;
import com.codedu.services.UserService; // Bunu kendi projene göre uyarla
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
    private Button btnChangePassword;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnRemoveAccount;

    private User user;
    private boolean darkMode = true;

    // Callbacks
    private Runnable themeToggleCallback;
    private Runnable onLogoutCallback; // Çıkış yapıldığında Login ekranına dönmek için

    // Backend iletişimi için UserService'i içeri alıyoruz (Constructor Injection)
    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    public void setUserModel(User user) {
        this.user = user;
        updateThemeButton();
    }

    public void setThemeToggleCallback(Runnable callback) {
        this.themeToggleCallback = callback;
    }

    public void setOnLogoutCallback(Runnable onLogoutCallback) {
        this.onLogoutCallback = onLogoutCallback;
    }

    @FXML
    public void initialize() {
        btnThemeToggle.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);

        btnThemeToggle.setOnAction(e -> {
            darkMode = !darkMode;
            updateThemeButton();
            if (themeToggleCallback != null)
                themeToggleCallback.run();
        });

        // Butonlara tıklandığında dialogları aç
        btnChangePassword.setOnAction(e -> showChangePasswordDialog());
        btnLogout.setOnAction(e -> showLogoutDialog());
        btnRemoveAccount.setOnAction(e -> showRemoveAccountDialog());

        // Tehlikeli işlemleri kırmızı (Danger) yapmak UI açısından iyidir
        btnRemoveAccount.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
    }

    private void updateThemeButton() {
        btnThemeToggle.setText(darkMode ? "Switch to light mode" : "Switch to dark mode");
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
        title.getStyleClass().add(Styles.TITLE_3);

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
        feedback.setWrapText(true);
        feedback.setAlignment(Pos.CENTER);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Password");
        saveBtn.getStyleClass().add(Styles.SUCCESS);

        saveBtn.setOnAction(e -> {
            String oldP = oldPwd.getText();
            String newP = newPwd.getText();
            String confP = confirmPwd.getText();

            if (oldP.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
                feedback.setText("Please fill in all fields.");
                feedback.setStyle("-fx-text-fill: red;");
            } else if (!newP.equals(confP)) {
                feedback.setText("New passwords do not match.");
                feedback.setStyle("-fx-text-fill: red;");
            } else if (newP.length() < 6) {
                feedback.setText("Password must be at least 6 characters.");
                feedback.setStyle("-fx-text-fill: red;");
            } else {
                // GERÇEK BACKEND İŞLEMİ BURADA YAPILIYOR
                try {
                    // Varsayım: UserService içinde changePassword adında bir metod var
                    boolean isChanged = userService.changePassword(user, oldP, newP);

                    if (isChanged) {
                        feedback.setText("✓ Password changed successfully!");
                        feedback.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        saveBtn.setDisable(true);
                        // İşlem bitince kutuları temizle
                        oldPwd.clear(); newPwd.clear(); confirmPwd.clear();
                    } else {
                        feedback.setText("Incorrect current password.");
                        feedback.setStyle("-fx-text-fill: red;");
                    }
                } catch (Exception ex) {
                    feedback.setText("An error occurred: " + ex.getMessage());
                    feedback.setStyle("-fx-text-fill: red;");
                }
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
        title.getStyleClass().add(Styles.TITLE_3);

        Label msg = new Label("Are you sure you want to log out?");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("Log Out");
        confirmBtn.getStyleClass().add(Styles.ACCENT);

        confirmBtn.setOnAction(e -> {
            dialog.close();
            // Kullanıcıyı giriş ekranına yönlendir
            if (onLogoutCallback != null) {
                onLogoutCallback.run();
            }
        });

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
        title.getStyleClass().add(Styles.TITLE_3);

        Label msg = new Label(
                "This action is permanent and cannot be undone.\nAll your progress, tokens, and purchases will be lost.");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button deleteBtn = new Button("Delete My Account");
        deleteBtn.getStyleClass().add(Styles.DANGER);

        deleteBtn.setOnAction(e -> {
            try {
                // GERÇEK BACKEND İŞLEMİ (Veritabanından uçurma)
                userService.deleteUser(user);

                dialog.close();
                // Hesap silindiği için zorunlu çıkış yapıp giriş ekranına atıyoruz
                if (onLogoutCallback != null) {
                    onLogoutCallback.run();
                }
            } catch (Exception ex) {
                System.err.println("Hesap silinirken hata oluştu: " + ex.getMessage());
            }
        });

        btnRow.getChildren().addAll(cancelBtn, deleteBtn);
        content.getChildren().addAll(title, msg, btnRow);

        dialog.setScene(new Scene(content));
        dialog.showAndWait();
    }

    private Stage createDialog(String title, int width, int height) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED); // Modern ve çerçevesiz görünüm
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
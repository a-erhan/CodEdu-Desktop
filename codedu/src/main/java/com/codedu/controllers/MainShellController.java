package com.codedu.controllers;

import com.codedu.models.Chapter;
import com.codedu.models.User;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for the main application shell.
 * Manages sidebar navigation, header data binding, and center content
 * switching.
 */
public class MainShellController {

    // --- Header elements ---
    @FXML
    private Label badgeLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label tokenLabel;
    @FXML
    private ProgressBar xpProgressBar;
    @FXML
    private Label xpLabel;

    // --- Sidebar buttons ---
    @FXML
    private Button btnLearningPath;
    @FXML
    private Button btnDailyChallenge;
    @FXML
    private Button btnLeaderboard;
    @FXML
    private Button btnForum;
    @FXML
    private Button btnStore;
    @FXML
    private Button btnAskAI;
    @FXML
    private Button btnProfile;
    @FXML
    private Button btnSettings;

    // --- Center content area ---
    @FXML
    private StackPane contentArea;

    private User user = new User();
    private Button activeButton;

    /**
     * Set the authenticated user from the login screen.
     */
    public void setUser(User user) {
        this.user = user;
        updateHeader();
    }

    @FXML
    public void initialize() {
        // Bind header to user model
        updateHeader();

        // Setup Learning Path button — loads full FXML module
        setupNavButtonWithHover(btnLearningPath);
        btnLearningPath.setOnAction(e -> {
            setActiveButton(btnLearningPath);
            loadLearningPath();
        });

        // Setup other sidebar navigation handlers (placeholders)
        setupNavButton(btnDailyChallenge, "\u2694\uFE0F Daily Challenge & Matchmaking",
                "Take on daily coding challenges or compete head-to-head with other students!");
        setupNavButton(btnLeaderboard, "\uD83C\uDFC6 Leaderboard",
                "See how you rank against your peers. Climb the ranks and earn exclusive badges!");
        setupNavButton(btnForum, "\uD83D\uDCAC Forum",
                "Discuss problems, share solutions, and collaborate with the CodEdu community.");

        // Store — loads full FXML module
        setupNavButtonWithHover(btnStore);
        btnStore.setOnAction(e -> {
            setActiveButton(btnStore);
            loadStore();
        });

        // Ask AI — placeholder
        setupNavButton(btnAskAI, "\uD83E\uDD16 Ask AI",
                "Get instant help from our AI tutor.\nAsk questions, debug your code, or explore concepts interactively.");

        // Profile — loads full FXML module
        setupNavButtonWithHover(btnProfile);
        btnProfile.setOnAction(e -> {
            setActiveButton(btnProfile);
            loadProfile();
        });

        // Settings — loads full FXML module
        setupNavButtonWithHover(btnSettings);
        btnSettings.setOnAction(e -> {
            setActiveButton(btnSettings);
            loadSettings();
        });

        // Show welcome screen by default
        showWelcome();
        setActiveButton(btnLearningPath);
    }

    private void updateHeader() {
        badgeLabel.setText("\uD83E\uDDD1\u200D\uD83D\uDCBB");
        usernameLabel.setText(user.getUsername() != null ? user.getUsername() : "User");
        tokenLabel.setText("\uD83E\uDE99 " + user.getTokenBalance() + " Tokens");
        xpProgressBar.setProgress(0);
        xpLabel.setText("XP: 0 / 1000");
    }

    private void setupNavButtonWithHover(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void setupNavButton(Button button, String sectionTitle, String sectionDescription) {
        setupNavButtonWithHover(button);
        button.setOnAction(e -> {
            setActiveButton(button);
            showSectionPlaceholder(sectionTitle, sectionDescription);
        });
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        activeButton = button;
        activeButton.getStyleClass().add("nav-button-active");
    }

    // --- Module loaders ---

    private void loadLearningPath() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/LearningPath.fxml"));
            Parent learningPathView = loader.load();
            LearningPathController lpController = loader.getController();
            lpController.setOnStartChapter(chapter -> loadChapterView(chapter));
            contentArea.getChildren().setAll(learningPathView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("\uD83D\uDCDA Learning Path",
                    "Error loading Learning Path module: " + ex.getMessage());
        }
    }

    private void loadChapterView(Chapter chapter) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/ChapterView.fxml"));
            Parent chapterView = loader.load();
            ChapterViewController controller = loader.getController();
            controller.setChapter(chapter);
            controller.setOnBack(() -> loadLearningPath());
            contentArea.getChildren().setAll(chapterView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("\uD83D\uDCDA Chapter",
                    "Error loading chapter: " + ex.getMessage());
        }
    }

    private void loadStore() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Store.fxml"));
            Parent storeView = loader.load();
            StoreController controller = loader.getController();
            controller.setUserModel(user);
            contentArea.getChildren().setAll(storeView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("\uD83D\uDED2 Store",
                    "Error loading Store module: " + ex.getMessage());
        }
    }

    private void loadProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Profile.fxml"));
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setUserModel(user);
            contentArea.getChildren().setAll(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("\uD83D\uDC64 Profile",
                    "Error loading Profile module: " + ex.getMessage());
        }
    }

    private void loadSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Settings.fxml"));
            Parent settingsView = loader.load();
            SettingsController controller = loader.getController();
            controller.setUserModel(user);
            controller.setThemeToggleCallback(() -> toggleTheme());
            contentArea.getChildren().setAll(settingsView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("\u2699\uFE0F Settings",
                    "Error loading Settings module: " + ex.getMessage());
        }
    }

    private void toggleTheme() {
        var scene = contentArea.getScene();
        if (scene == null)
            return;

        String darkCSS = getClass().getResource("/com/codedu/views/application.css").toExternalForm();
        String lightCSS = getClass().getResource("/com/codedu/views/application-light.css").toExternalForm();

        scene.getStylesheets().clear();
        // Default to dark mode
        scene.getStylesheets().add(darkCSS);
    }

    // --- Content helpers ---

    private void showWelcome() {
        VBox welcomeBox = new VBox(16);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setMaxWidth(600);

        Label logo = new Label("\uD83D\uDE80");
        logo.setStyle("-fx-font-size: 64px;");

        Label title = new Label("Welcome to CodEdu");
        title.getStyleClass().add("welcome-title");

        Label subtitle = new Label(
                "Level up your coding skills through gamified learning.\nSelect a module from the sidebar to begin your journey.");
        subtitle.getStyleClass().add("welcome-subtitle");
        subtitle.setWrapText(true);

        Label stats = new Label(String.format("\u26A1 %s  |  \uD83E\uDE99 %d Tokens  |  \uD83C\uDFAF 0 / 1000 XP",
                user.getUsername() != null ? user.getUsername() : "User", user.getTokenBalance()));
        stats.getStyleClass().add("welcome-stats");

        welcomeBox.getChildren().addAll(logo, title, subtitle, stats);
        contentArea.getChildren().setAll(welcomeBox);
    }

    private void showSectionPlaceholder(String title, String description) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(500);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("section-description");
        descLabel.setWrapText(true);

        Label comingSoon = new Label("\uD83D\uDEA7 Module coming soon\u2026");
        comingSoon.getStyleClass().add("coming-soon");

        box.getChildren().addAll(titleLabel, descLabel, comingSoon);
        contentArea.getChildren().setAll(box);
    }
}
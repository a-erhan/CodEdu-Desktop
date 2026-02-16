package com.codedu.controllers;

import com.codedu.models.UserModel;
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

    // --- Center content area ---
    @FXML
    private StackPane contentArea;

    private final UserModel user = new UserModel();
    private Button activeButton;

    @FXML
    public void initialize() {
        // Bind header to user model
        badgeLabel.setText(user.getBadgeIcon());
        usernameLabel.setText(user.getUsername());
        tokenLabel.setText("\uD83E\uDE99 " + user.getTokenBalance() + " Tokens");
        xpProgressBar.setProgress(user.getXPProgress());
        xpLabel.setText("XP: " + user.getCurrentXP() + " / " + user.getMaxXP());

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
        setupNavButton(btnStore, "\uD83D\uDED2 Store",
                "Spend your hard\u2011earned tokens on themes, power\u2011ups, and profile customizations.");

        // Show welcome screen by default
        showWelcome();

        // Set first button as active
        setActiveButton(btnLearningPath);
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

    private void loadLearningPath() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/LearningPath.fxml"));
            Parent learningPathView = loader.load();
            contentArea.getChildren().setAll(learningPathView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("\uD83D\uDCDA Learning Path",
                    "Error loading Learning Path module: " + ex.getMessage());
        }
    }

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

        Label stats = new Label(String.format("\u26A1 %s  |  \uD83E\uDE99 %d Tokens  |  \uD83C\uDFAF %d / %d XP",
                user.getUsername(), user.getTokenBalance(), user.getCurrentXP(), user.getMaxXP()));
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

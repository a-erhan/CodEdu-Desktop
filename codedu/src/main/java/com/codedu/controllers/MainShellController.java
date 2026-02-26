package com.codedu.controllers;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Styles;
import com.codedu.models.Chapter;
import com.codedu.models.Competitor;
import com.codedu.models.DailyChallenge;
import com.codedu.models.ForumPost;
import com.codedu.models.User;
import com.codedu.models.UserGameState;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.io.IOException;

/**
 * Shell-only controller: sidebar/nav wiring, header (tokens, XP, profile icon, theme),
 * high-level routing (loadLearningPath, loadDailyChallenge, loadForum, loadAskAI, etc.),
 * and initialization of shared models (user, gameState) for header and profile.
 * Feature-specific UI logic lives in the respective page controllers.
 */
@Controller
public class MainShellController {

    // ========== FXML: Header ==========
    @FXML
    private Label badgeLabel;
    @FXML
    private Label tokenLabel;
    @FXML
    private ProgressBar xpProgressBar;
    @FXML
    private Label xpLabel;
    @FXML
    private Label welcomeNavLabel;
    @FXML
    private Label profileIconLabel;

    // ========== FXML: Sidebar ==========
    @FXML
    private VBox sidebar;
    @FXML
    private Label taglineLabel;

    // ========== FXML: Sidebar buttons ==========
    @FXML
    private Button btnLearningPath;
    @FXML
    private Button btnDailyChallenge;
    @FXML
    private Button btnAchievements;
    @FXML
    private Button btnLeaderboard;
    @FXML
    private Button btnForum;
    @FXML
    private Button btnStore;
    @FXML
    private Button btnMatchmaking;
    @FXML
    private Button btnAskAI;
    @FXML
    private Button btnSettings;

    // ========== FXML: Content area ==========
    @FXML
    private StackPane contentArea;

    @FXML
    private ScrollPane sidebarScroll;

    // ========== Shared state (shell: header + profile) ==========
    private User user = new User();
    private UserGameState gameState;
    private Button activeButton;
    private boolean darkTheme = true;

    // ========== Lifecycle ==========

    /** Called from login/register when user is set. */
    public void setUser(User user) {
        this.user = user;
        initDemoModelsIfNeeded();
        updateHeader();
    }

    @FXML
    public void initialize() {
        initDemoModelsIfNeeded();
        updateHeader();
        initSidebarAndHeaderStyles();
        styleAndWireNavigation();
        ensureShellFillsScene();
        setActiveButton(btnLearningPath);
        loadLearningPath();
    }

    /** Make root pane and sidebar fill available height so the shell reaches the bottom. */
    private void ensureShellFillsScene() {
        Platform.runLater(() -> {
            if (contentArea == null || contentArea.getScene() == null) return;
            javafx.scene.Node root = contentArea.getScene().getRoot();
            if (root instanceof Region) {
                Region r = (Region) root;
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
        });
    }

    /** Set content area to the given view and make it fill the center. */
    private void setContentAndFill(Parent view) {
        contentArea.getChildren().setAll(view);
        if (view instanceof Region) {
            Region r = (Region) view;
            r.setMaxWidth(Double.MAX_VALUE);
            r.setMaxHeight(Double.MAX_VALUE);
        }
    }

    /** Sidebar, tagline, welcome label, profile icon. */
    private void initSidebarAndHeaderStyles() {
        if (taglineLabel != null) {
            taglineLabel.getStyleClass().add(Styles.TITLE_2);
        }
        if (sidebar != null) {
            sidebar.setSpacing(16);
            sidebar.setFillWidth(true);
            sidebar.getStyleClass().addAll(Styles.BG_SUBTLE, Styles.ELEVATED_1, Styles.ROUNDED);
        }
        if (sidebarScroll != null) {
            sidebarScroll.setMaxHeight(Double.MAX_VALUE);
            sidebarScroll.setMinHeight(0);
            sidebarScroll.getStyleClass().addAll(Styles.BG_SUBTLE, Styles.ELEVATED_1, Styles.ROUNDED);
        }
        if (welcomeNavLabel != null) {
            welcomeNavLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        }
        if (profileIconLabel != null) {
            profileIconLabel.setMinSize(32, 32);
            profileIconLabel.setPrefSize(32, 32);
            profileIconLabel.setMaxSize(32, 32);
            profileIconLabel.setAlignment(Pos.CENTER);
            profileIconLabel.setShape(new Circle(16));
            profileIconLabel.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.INTERACTIVE);
            profileIconLabel.setOnMouseClicked(e -> loadProfile());
        }
    }

    /** Style all nav buttons and set their actions. */
    private void styleAndWireNavigation() {
        styleNavButton(btnLearningPath);
        styleNavButton(btnDailyChallenge);
        styleNavButton(btnAchievements);
        styleNavButton(btnLeaderboard);
        styleNavButton(btnForum);
        styleNavButton(btnMatchmaking);
        styleNavButton(btnStore);
        styleNavButton(btnAskAI);
        styleNavButton(btnSettings);

        // Setup Learning Path button — loads full FXML module
        setupNavButtonWithHover(btnLearningPath);
        btnLearningPath.setOnAction(e -> {
            setActiveButton(btnLearningPath);
            loadLearningPath();
        });

        // Daily challenge & matchmaking
        setupNavButtonWithHover(btnDailyChallenge);
        btnDailyChallenge.setOnAction(e -> {
            setActiveButton(btnDailyChallenge);
            loadDailyChallenge();
        });

        // Achievements
        setupNavButtonWithHover(btnAchievements);
        btnAchievements.setOnAction(e -> {
            setActiveButton(btnAchievements);
            loadAchievements();
        });

        // Leaderboard
        setupNavButtonWithHover(btnLeaderboard);
        btnLeaderboard.setOnAction(e -> {
            setActiveButton(btnLeaderboard);
            loadLeaderboard();
        });

        // Forum
        setupNavButtonWithHover(btnForum);
        btnForum.setOnAction(e -> {
            setActiveButton(btnForum);
            loadForum();
        });

        // Matchmaking
        setupNavButtonWithHover(btnMatchmaking);
        btnMatchmaking.setOnAction(e -> {
            setActiveButton(btnMatchmaking);
            loadMatchmaking();
        });

        // Store — loads full FXML module
        setupNavButtonWithHover(btnStore);
        btnStore.setOnAction(e -> {
            setActiveButton(btnStore);
            loadStore();
        });

        // Ask AI
        setupNavButtonWithHover(btnAskAI);
        btnAskAI.setOnAction(e -> {
            setActiveButton(btnAskAI);
            loadAskAI();
        });

        // Settings — loads full FXML module
        setupNavButtonWithHover(btnSettings);
        btnSettings.setOnAction(e -> {
            setActiveButton(btnSettings);
            loadSettings();
        });
    }

    // ========== Header ==========

    private void updateHeader() {
        String username = user.getUsername() != null ? user.getUsername() : "User";
        badgeLabel.setText("");
        tokenLabel.setText("Tokens: " + user.getTokenBalance());

        if (welcomeNavLabel != null) {
            welcomeNavLabel.setText("Welcome @" + username);
            welcomeNavLabel.setOnMouseClicked(e -> loadProfile());
        }
        if (profileIconLabel != null) {
            String initial = username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase();
            profileIconLabel.setText(initial);
        }

        int level = gameState != null ? gameState.getLevel() : 1;
        int xp = gameState != null ? gameState.getXp() : 0;
        int levelCap = Math.max(1, level * 1000);
        double progress = Math.max(0, Math.min(1, (double) xp / levelCap));
        xpProgressBar.setProgress(progress);
        xpLabel.setText("XP: " + xp + " / " + levelCap);
    }

    // ========== Shared demo data (shell: header + profile only) ==========

    private void initDemoModelsIfNeeded() {
        if (gameState == null) {
            gameState = UserGameState.builder()
                    .user(user)
                    .level(1)
                    .xp(0)
                    .heartCount(3)
                    .build();
        }
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

    // ========== Navigation helpers ==========

    private void styleNavButton(Button button) {
        if (button == null) return;
        button.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED, Styles.DENSE, Styles.INTERACTIVE);
        button.setMaxWidth(Double.MAX_VALUE);
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove(Styles.ACCENT);
        }
        activeButton = button;
        if (!activeButton.getStyleClass().contains(Styles.ACCENT)) {
            activeButton.getStyleClass().add(Styles.ACCENT);
        }
    }

    // ========== Page loaders ==========

    private void loadLearningPath() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/LearningPath.fxml"));
            Parent learningPathView = loader.load();
            LearningPathController lpController = loader.getController();
            lpController.setOnStartChapter(chapter -> loadChapterView(chapter));
            setContentAndFill(learningPathView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Learning path",
                    "Error loading learning path module: " + ex.getMessage());
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
            setContentAndFill(chapterView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Chapter",
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
            setContentAndFill(storeView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Store",
                    "Error loading store module: " + ex.getMessage());
        }
    }

    private void loadProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Profile.fxml"));
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setViewingSelf(true);
            controller.setUserModel(user);
            controller.setGameState(gameState);
            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile",
                    "Error loading profile module: " + ex.getMessage());
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
            setContentAndFill(settingsView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Settings",
                    "Error loading settings module: " + ex.getMessage());
        }
    }

    private void loadDailyChallenge() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/DailyChallenge.fxml"));
            Parent view = loader.load();
            DailyChallengeController controller = loader.getController();
            controller.setOnStartChallenge(this::openChallengePage);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Daily challenges",
                    "Error loading daily challenges module: " + ex.getMessage());
        }
    }

    private void loadAchievements() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Achievements.fxml"));
            Parent view = loader.load();
            AchievementsController controller = loader.getController();
            controller.setCurrentUser(user);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Achievements",
                    "Error loading achievements module: " + ex.getMessage());
        }
    }

    private void loadForum() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Forum.fxml"));
            Parent view = loader.load();
            ForumController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setOnOpenPost(this::openForumPost);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Forum",
                    "Error loading forum module: " + ex.getMessage());
        }
    }

    private void loadMatchmaking() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Matchmaking.fxml"));
            Parent view = loader.load();
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Matchmaking",
                    "Error loading matchmaking module: " + ex.getMessage());
        }
    }

    private void loadAskAI() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/AskAI.fxml"));
            Parent view = loader.load();
            AskAIController controller = loader.getController();
            controller.setRemainingRequests(3);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Ask AI",
                    "Error loading Ask AI module: " + ex.getMessage());
        }
    }

    private void loadLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Leaderboard.fxml"));
            Parent view = loader.load();
            LeaderboardController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setOnOpenProfile(this::openCompetitorProfile);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Leaderboard",
                    "Error loading leaderboard module: " + ex.getMessage());
        }
    }

    // ========== Routing: open* (from child controllers) ==========
    // Shell only loads view and passes data/callbacks; no feature logic here.

    private void openCompetitorProfile(Competitor competitor, java.util.List<Competitor> competitorOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Profile.fxml"));
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setCompetitor(competitor, competitorOrder);
            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile",
                    "Error loading competitor profile: " + ex.getMessage());
        }
    }

    private void openForumPost(ForumPost post) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/ForumPost.fxml"));
            Parent view = loader.load();
            ForumPostController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setPost(post);
            controller.setOnBack(() -> {
                setActiveButton(btnForum);
                loadForum();
            });
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Forum post",
                    "Error loading post: " + ex.getMessage());
        }
    }

    private void openChallengePage(DailyChallenge challenge) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Challenge.fxml"));
            Parent view = loader.load();
            ChallengeController controller = loader.getController();
            controller.setChallenge(challenge);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Challenge",
                    "Error opening challenge: " + ex.getMessage());
        }
    }

    // ========== Theme ==========

    private void toggleTheme() {
        darkTheme = !darkTheme;
        if (darkTheme) {
            Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
        }
    }

    // ========== Fallback (loader errors) ==========

    private void showSectionPlaceholder(String title, String description) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(500);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("section-description");
        descLabel.setWrapText(true);

        Label comingSoon = new Label("Module coming soon\u2026");
        comingSoon.getStyleClass().add("coming-soon");

        box.getChildren().addAll(titleLabel, descLabel, comingSoon);
        setContentAndFill(box);
    }
}
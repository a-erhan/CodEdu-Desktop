package com.codedu.controllers;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Styles;
import com.codedu.models.learning.Chapter;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.social.ForumPost;
import com.codedu.models.learning.Question;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.io.IOException;

/**
 * Shell-only controller: sidebar/nav wiring, header (tokens, XP, profile icon,
 * theme),
 * high-level routing (loadLearningPath, loadDailyChallenge, loadForum,
 * loadAskAI, etc.),
 * and initialization of shared models (user, gameState) for header and profile.
 * Feature-specific UI logic lives in the respective page controllers.
 */
@Controller
public class MainShellController {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @org.springframework.beans.factory.annotation.Autowired
    private com.codedu.repositories.interfaces.UserRepository userRepository;

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
    @FXML
    private VBox sidebarContainer;

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

    /**
     * Make root pane and sidebar fill available height so the shell reaches the
     * bottom.
     */
    private void ensureShellFillsScene() {
        Platform.runLater(() -> {
            if (contentArea == null || contentArea.getScene() == null)
                return;
            javafx.scene.Scene scene = contentArea.getScene();
            javafx.scene.Node root = scene.getRoot();
            if (root instanceof Region) {
                Region r = (Region) root;
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
            // Force sidebar to match center height so it fills the full left column
            if (sidebarScroll != null && contentArea != null) {
                sidebarScroll.prefHeightProperty().unbind();
                sidebarScroll.prefHeightProperty().bind(contentArea.heightProperty());
                sidebarScroll.minHeightProperty().unbind();
                sidebarScroll.minHeightProperty().bind(contentArea.heightProperty());
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
        if (sidebarContainer != null) {
            sidebarContainer.getStyleClass().addAll(Styles.BG_SUBTLE, Styles.ELEVATED_1, Styles.ROUNDED);
        }
        if (sidebarScroll != null) {
            sidebarScroll.setMaxHeight(Double.MAX_VALUE);
            sidebarScroll.setMinHeight(0);
        }
        // So the spacer (VBox.vgrow=ALWAYS) can expand and push version to bottom
        if (sidebar != null && sidebarScroll != null) {
            sidebar.minHeightProperty().bind(sidebarScroll.heightProperty());
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
        if (xpProgressBar != null) {
            xpProgressBar.setMinHeight(14);
            xpProgressBar.setPrefHeight(14);
            xpProgressBar.setMaxHeight(14);

            xpProgressBar.setMinWidth(280);
            xpProgressBar.setPrefWidth(280);
            xpProgressBar.setMaxWidth(280);

            xpProgressBar.getStyleClass().addAll(Styles.MEDIUM, Styles.ROUNDED);
            xpProgressBar.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");

            VBox.setMargin(xpProgressBar, new javafx.geometry.Insets(10, 0, 0, 0));

            if (xpProgressBar.getParent() instanceof VBox) {
                ((VBox) xpProgressBar.getParent()).setAlignment(Pos.CENTER);
            }
        }

        if (tokenLabel != null) {
            tokenLabel.setPadding(new javafx.geometry.Insets(6, 16, 6, 16));
            tokenLabel.setStyle("-fx-background-color: rgba(184, 134, 11, 0.12); " +
                    "-fx-background-radius: 20; " +
                    "-fx-font-size: 15px;");
        }

        if (badgeLabel != null) {
            badgeLabel.setPadding(new javafx.geometry.Insets(2, 10, 2, 10));
            badgeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD, Styles.ROUNDED);
            badgeLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); " +
                    "-fx-border-color: rgba(255, 255, 255, 0.15); " +
                    "-fx-border-radius: 10; -fx-background-radius: 10; " +
                    "-fx-text-fill: -color-fg-muted;");
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
        String username = user.getUsername() != null && !user.getUsername().isEmpty()
                ? user.getUsername()
                : "User";

        int tokens = 0, level = 1, xp = 0;

        if (this.gameState != null) {
            tokens = gameState.getTokenBalance();
            level = gameState.getLevel();
            xp = gameState.getXp();
        } else if (user != null && user.getGameState() != null) {
            tokens = user.getGameState().getTokenBalance();
            level = user.getGameState().getLevel();
            xp = user.getGameState().getXp();
        }

        tokenLabel.setText("Tokens: " + tokens);
        tokenLabel.setStyle(tokenLabel.getStyle() + "-fx-text-fill: #B8860B; -fx-font-weight: bold;");

        javafx.scene.shape.Circle goldCircle = new javafx.scene.shape.Circle(5);
        goldCircle.setFill(javafx.scene.paint.Color.web("#B8860B"));
        goldCircle.setStroke(javafx.scene.paint.Color.web("#8B6508"));
        goldCircle.setStrokeWidth(1.2);
        tokenLabel.setGraphic(goldCircle);
        tokenLabel.setGraphicTextGap(10);

        if (badgeLabel != null) {
            badgeLabel.setText("Lvl " + level);
        }

        int levelCap = Math.max(1, level * 1000);
        double progress = (double) xp / levelCap;
        xpProgressBar.setProgress(progress);

        xpProgressBar.getStyleClass().removeAll("success", "danger", "warning");
        if (!xpProgressBar.getStyleClass().contains(Styles.ACCENT)) {
            xpProgressBar.getStyleClass().add(Styles.ACCENT);
        }
        if (!xpProgressBar.getStyleClass().contains(Styles.ROUNDED)) {
            xpProgressBar.getStyleClass().add(Styles.ROUNDED);
        }

        xpLabel.setText("XP: " + xp + " / " + levelCap);
        xpLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 13px;");

        if (welcomeNavLabel != null)
            welcomeNavLabel.setText("@" + username);
        if (profileIconLabel != null) {
            String initial = username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase();
            profileIconLabel.setText(initial);
            profileIconLabel.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: white;");
        }
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
            if (user != null) {
                user.setGameState(gameState);
            }
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
        if (button == null)
            return;
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
            loader.setControllerFactory(applicationContext::getBean);
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
            loader.setControllerFactory(applicationContext::getBean);
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
            loader.setControllerFactory(applicationContext::getBean);
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
            loader.setControllerFactory(applicationContext::getBean);
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setViewingSelf(true);
            controller.setOnProfileClick(this::openUserProfile);
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
            loader.setControllerFactory(applicationContext::getBean);
            Parent settingsView = loader.load();
            SettingsController controller = loader.getController();
            controller.setUserModel(user);
            controller.setThemeToggleCallback(() -> toggleTheme());

            // Logout and Account Removal callback
            controller.setOnLogoutCallback(() -> logout());

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
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            DailyChallengeController controller = loader.getController();
            controller.setOnStartQuestion(this::openChallengePage);
            controller.setOnBack(this::loadLearningPath);
            setContentAndFill(view);
        } catch (Exception ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Daily challenges",
                    "Error loading daily challenges module: " + ex.getMessage());
        }
    }

    private void loadAchievements() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Achievements.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
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
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            ForumController controller = loader.getController();
            controller.setCurrentUser(user);

            controller.setOnOpenPost(this::openForumPost);
            controller.setOnOpenProfile(
                    username -> userRepository.findByUsername(username).ifPresent(this::openUserProfile));
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
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            MatchmakingController controller = loader.getController();
            controller.setCurrentUser(user);   // triggers real queue join via WebSocket
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
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            AIChatbotController controller = loader.getController();
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
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            LeaderboardController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setOnOpenProfile(this::openCompetitorProfile);
            setContentAndFill(view);
        } catch (Exception ex) {
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
            loader.setControllerFactory(applicationContext::getBean);
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setCompetitor(competitor, competitorOrder);
            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile",
                    "Error loading competitor profile: " + ex.getMessage());
        }
    }

    private void openUserProfile(User profileUser) {
        if (profileUser == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Profile.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();

            boolean isSelf = (user != null && user.getId() > 0
                    && profileUser != null && user.getId() == profileUser.getId());
            controller.setCurrentUser(user);
            controller.setViewingSelf(isSelf);
            controller.setOnProfileClick(this::openUserProfile);
            controller.setUserModel(profileUser);

            UserGameState state = null;
            try {
                // Try reading gameState (might throw LazyInitializationException)
                state = profileUser.getGameState();
            } catch (Exception ignored) {
            }
            if (state == null) {
                state = UserGameState.builder()
                        .user(profileUser)
                        .level(1)
                        .xp(0)
                        .heartCount(3)
                        .build();
            }
            controller.setGameState(state);

            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile",
                    "Error loading profile view: " + ex.getMessage());
        }
    }

    private void openForumPost(Integer postId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/ForumPost.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();

            ForumPostController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setPostId(postId);
            controller.setOnOpenProfile(
                    username -> userRepository.findByUsername(username).ifPresent(this::openUserProfile));
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

    private void openChallengePage(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/QuestionSolver.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            QuestionSolverController controller = loader.getController();
            controller.setQuestion(question);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Question Solver",
                    "Error opening question solver: " + ex.getMessage());
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

    private void showSectionPlaceholder(String title, String subtitle) {
        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add(Styles.TITLE_3);
        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        placeholder.getChildren().addAll(lblTitle, lblSubtitle);
        setContentAndFill(placeholder);
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/codedu/views/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
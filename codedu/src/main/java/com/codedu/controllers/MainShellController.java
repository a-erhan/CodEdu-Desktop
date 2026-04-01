package com.codedu.controllers;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Styles;
import com.codedu.models.learning.Chapter;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.learning.Question;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.services.interfaces.ChatWindowManager;
import com.codedu.services.interfaces.UserChapterProgressService;
import com.codedu.services.interfaces.UserService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import java.io.IOException;

@Controller
public class MainShellController {

    @Autowired
    private ApplicationContext applicationContext;

    @org.springframework.beans.factory.annotation.Autowired
    private UserChapterProgressService progressService;

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

    @Autowired
    private ChatWindowManager chatWindowManager;

    @Autowired
    private UserService userService;

    @FXML
    private Label taglineLabel;
    @FXML
    private VBox sidebar, sidebarContainer;
    @FXML
    private ScrollPane sidebarScroll;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnLearningPath, btnDailyChallenge, btnAchievements, btnLeaderboard, btnForum, btnStore,
            btnInventory, btnMatchmaking, btnAskAI, btnSettings;

    private User user = new User();
    private UserGameState gameState;
    private Button activeButton;
    private boolean darkTheme = true;

    public void setUser(User user) {
        this.user = user;
        this.gameState = null;
        if (initDemoModelsIfNeeded()) {
            userService.saveUser(user);
        }
        updateHeader();
        chatWindowManager.connectUser(user);
        Platform.runLater(() -> {
            setActiveButton(btnLearningPath);
            loadLearningPath();
        });
    }

    @FXML
    public void initialize() {
        initSidebarAndHeaderStyles();
        styleAndWireNavigation();
        ensureShellFillsScene();
        setActiveButton(btnLearningPath);
        showSectionPlaceholder("Loading", "Preparing your workspace…");
    }

    private void ensureShellFillsScene() {
        Platform.runLater(() -> {
            if (contentArea == null || contentArea.getScene() == null)
                return;
            javafx.scene.Node root = contentArea.getScene().getRoot();
            if (root instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
            if (sidebarScroll != null && contentArea != null) {
                sidebarScroll.prefHeightProperty().bind(contentArea.heightProperty());
                sidebarScroll.minHeightProperty().bind(contentArea.heightProperty());
            }
        });
    }

    private void setContentAndFill(Parent view) {
        contentArea.getChildren().setAll(view);
        if (view instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            r.setMaxHeight(Double.MAX_VALUE);
        }
    }

    private void initSidebarAndHeaderStyles() {
        if (taglineLabel != null)
            taglineLabel.getStyleClass().add(Styles.TITLE_2);
        if (sidebar != null) {
            sidebar.setSpacing(16);
            sidebar.getStyleClass().addAll(Styles.BG_SUBTLE, Styles.ELEVATED_1, Styles.ROUNDED);
            if (sidebarScroll != null)
                sidebar.minHeightProperty().bind(sidebarScroll.heightProperty());
        }
        if (profileIconLabel != null) {
            profileIconLabel.setShape(new Circle(16));
            profileIconLabel.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.INTERACTIVE);
            profileIconLabel.setOnMouseClicked(e -> loadProfile());
        }
        if (xpProgressBar != null) {
            xpProgressBar.getStyleClass().addAll(Styles.MEDIUM, Styles.ROUNDED);
        }
    }

    private void styleAndWireNavigation() {
        Button[] navButtons = { btnLearningPath, btnDailyChallenge, btnAchievements, btnLeaderboard, btnForum,
                btnMatchmaking, btnStore, btnInventory, btnAskAI, btnSettings };
        for (Button btn : navButtons) {
            styleNavButton(btn);
            setupNavButtonWithHover(btn);
        }

        btnLearningPath.setOnAction(e -> {
            setActiveButton(btnLearningPath);
            loadLearningPath();
        });
        btnDailyChallenge.setOnAction(e -> {
            setActiveButton(btnDailyChallenge);
            loadDailyChallenge();
        });
        btnAchievements.setOnAction(e -> {
            setActiveButton(btnAchievements);
            loadAchievements();
        });
        btnLeaderboard.setOnAction(e -> {
            setActiveButton(btnLeaderboard);
            loadLeaderboard();
        });
        btnForum.setOnAction(e -> {
            setActiveButton(btnForum);
            loadForum();
        });
        btnMatchmaking.setOnAction(e -> {
            setActiveButton(btnMatchmaking);
            loadMatchmaking();
        });
        btnStore.setOnAction(e -> {
            setActiveButton(btnStore);
            loadStore();
        });

        btnInventory.setOnAction(e -> {
            setActiveButton(btnInventory);
            loadInventory();
        });
        btnAskAI.setOnAction(e -> {
            setActiveButton(btnAskAI);
            loadAskAI();
        });
        btnSettings.setOnAction(e -> {
            setActiveButton(btnSettings);
            loadSettings();
        });
    }

    private void loadLearningPath() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/LearningPath.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            LearningPathController lpController = loader.getController();
            lpController.setOnStartChapter(this::loadChapterView);
            lpController.setCurrentUser(this.user);
            lpController.refreshPath();
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Learning path", "Error loading learning path module.");
        }
    }

    private void loadChapterView(Chapter chapter) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/ChapterView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent chapterView = loader.load();

            ChapterViewController controller = loader.getController();

            // 2. Fetch the progress record for this specific user and chapter
            // We use the 'user' object already present in this Shell controller
            com.codedu.models.learning.UserChapterProgress progress =
                    progressService.getProgress(this.user, chapter);

            // 3. Pass BOTH to the controller
            controller.setChapter(chapter, progress);

            controller.setOnBack(() -> loadLearningPath());
            setContentAndFill(chapterView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Chapter", "Error loading chapter view.");
        }
    }

    private void loadDailyChallenge() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/DailyChallenge.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            DailyChallengeController controller = loader.getController();
            controller.setOnStartQuestion(this::openChallengePage);
            controller.setOnBack(this::loadLearningPath);
            setContentAndFill(view);
        } catch (Exception ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Daily challenges", "Error loading daily challenges module.");
        }
    }

    private void loadAchievements() {
        try {
            // FETCH FRESH DATA HERE
            User freshUser = userService.getUserWithProfileData(user.getUsername())
                    .orElse(this.user);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Achievements.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            AchievementsController controller = loader.getController();

            // Pass the fresh user with initialized achievements
            controller.setCurrentUser(freshUser);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Achievements", "Error loading achievements module.");
        }
    }

    private void loadForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Forum.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            ForumController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setOnOpenPost(this::openForumPost);
            controller.setOnOpenProfile(
                    username -> userService.getUserWithProfileData(username).ifPresent(this::openUserProfile));
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Forum", "Error loading forum module.");
        }
    }

    private void openForumPost(Integer postId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/ForumPost.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            ForumPostController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setPostId(postId);
            controller.setOnOpenProfile(
                    username -> userService.getUserWithProfileData(username).ifPresent(this::openUserProfile));
            controller.setOnBack(() -> {
                setActiveButton(btnForum);
                loadForum();
            });
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Forum post", "Error loading post.");
        }
    }

    private void openUserProfile(User profileUser) {
        if (profileUser == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Profile.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            boolean isSelf = (user != null && user.getId() > 0 && profileUser.getId() == user.getId());
            UserGameState state = profileUser.getGameState();
            if (state == null) {
                state = UserGameState.newDefault();
                profileUser.setGameState(state);
                userService.saveUser(profileUser);
            }
            controller.setCurrentUser(user);
            controller.setViewingSelf(isSelf);
            controller.setOnProfileClick(this::openUserProfile);
            controller.setUserModel(profileUser);
            controller.setGameState(state);
            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile", "Error loading profile view.");
        }
    }

    private void loadMatchmaking() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Matchmaking.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            MatchmakingController controller = loader.getController();
            controller.setCurrentUser(user);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Matchmaking", "Error loading matchmaking module.");
        }
    }

    private void loadStore() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Store.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            StoreController controller = loader.getController();
            controller.setUserModel(user);
            controller.setOnUserUpdated(updated -> {
                if (updated == null) {
                    return;
                }
                this.user = updated;
                this.gameState = updated.getGameState();
                updateHeader();
            });
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Store", "Error loading store module.");
        }
    }

    private void loadInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/InventoryItem.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            InventoryItemController controller = loader.getController();
            controller.setUserModel(user);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Inventory", "Error loading inventory.");
        }
    }

    private void loadAskAI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/AskAI.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            AIChatbotController controller = loader.getController();
            controller.setRemainingRequests(3);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Ask AI", "Error loading Ask AI module.");
        }
    }

    private void loadLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Leaderboard.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            LeaderboardController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setOnOpenProfile(this::openCompetitorProfile);
            setContentAndFill(view);
        } catch (Exception ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Leaderboard", "Error loading leaderboard module.");
        }
    }

    private void openCompetitorProfile(Competitor competitor, java.util.List<Competitor> competitorOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Profile.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setCompetitor(competitor, competitorOrder);
            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile", "Error loading competitor profile.");
        }
    }

    private void openChallengePage(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/QuestionSolver.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            QuestionSolverController controller = loader.getController();
            controller.setQuestion(question);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Question Solver", "Error opening question solver.");
        }
    }

    private void loadSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Settings.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            SettingsController controller = loader.getController();
            controller.setUserModel(user);
            controller.setThemeToggleCallback(this::toggleTheme);
            controller.setOnLogoutCallback(this::logout);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Settings", "Error loading settings.");
        }
    }

    private void loadProfile() {
        // Instead of openUserProfile(this.user), fetch first:
        userService.getUserWithProfileData(user.getUsername())
                .ifPresentOrElse(
                        this::openUserProfile,
                        () -> openUserProfile(this.user)
                );
    }

    private void updateHeader() {
        String username = (user.getUsername() != null && !user.getUsername().isEmpty()) ? user.getUsername() : "User";
        int tokens = (gameState != null) ? gameState.getTokenBalance() : 0;
        int level = (gameState != null) ? gameState.getLevel() : 1;
        int xp = (gameState != null) ? gameState.getXp() : 0;

        tokenLabel.setText("Tokens: " + tokens);
        badgeLabel.setText("Lvl " + level);
        welcomeNavLabel.setText("@" + username);

        int levelCap = Math.max(1, level * 1000);
        xpProgressBar.setProgress((double) xp / levelCap);
        xpLabel.setText("XP: " + xp + " / " + levelCap);

        if (profileIconLabel != null) {
            profileIconLabel.setText(username.substring(0, 1).toUpperCase());
            profileIconLabel.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: white;");
        }
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        Application.setUserAgentStylesheet(
                darkTheme ? new NordDark().getUserAgentStylesheet() : new NordLight().getUserAgentStylesheet());
    }

    private void setActiveButton(Button button) {
        if (activeButton != null)
            activeButton.getStyleClass().remove(Styles.ACCENT);
        activeButton = button;
        if (activeButton != null)
            activeButton.getStyleClass().add(Styles.ACCENT);
    }

    private void styleNavButton(Button button) {
        if (button != null)
            button.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED, Styles.DENSE, Styles.INTERACTIVE);
    }

    private void setupNavButtonWithHover(Button button) {
        if (button == null)
            return;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Login.fxml"));
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

    /**
     * @return true if a new {@link UserGameState} was attached and must be persisted (e.g. legacy users with no row).
     */
    private boolean initDemoModelsIfNeeded() {
        if (user == null) {
            return false;
        }
        if (user.getGameState() != null) {
            this.gameState = user.getGameState();
            return false;
        }
        this.gameState = UserGameState.newDefault();
        user.setGameState(gameState);
        return true;
    }
}
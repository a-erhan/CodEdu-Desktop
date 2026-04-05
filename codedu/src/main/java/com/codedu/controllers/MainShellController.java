package com.codedu.controllers;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Styles;
import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.dtos.learning.ChapterDTO;
import com.codedu.dtos.learning.QuestionDTO;
import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.CodeImplementationQuestion;
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

    @Autowired
    private UserChapterProgressService progressService;

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

    @Autowired
    private com.codedu.services.interfaces.UserGameStateService userGameStateService;

    @FXML
    private Label taglineLabel;
    @FXML
    private Label heartLabel;
    @FXML
    private Label streakLabel;
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

    private long lastShellUserRefreshMs;
    private static final long FOCUS_REFRESH_MIN_INTERVAL_MS = 900L;

    public void setUser(User user) {
        if (user == null) return;

        userService.getUserWithProfileData(user.getUsername()).ifPresentOrElse(
                freshUser -> {
                    this.user = freshUser;
                    if (initDemoModelsIfNeeded()) {
                        userService.saveUser(this.user);
                    }
                },
                () -> {
                    this.user = user;
                    if (initDemoModelsIfNeeded()) {
                        userService.saveUser(this.user);
                    }
                }
        );

        updateHeader();
        lastShellUserRefreshMs = System.currentTimeMillis();

        chatWindowManager.connectUser(this.user);

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

        // 1. Create Streak Label if it doesn't exist yet
        if (streakLabel == null) {
            streakLabel = new Label("🔥 0");
            streakLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
        }

        // 2. Create Heart Label if it doesn't exist yet
        if (heartLabel == null) {
            heartLabel = new Label("❤️ 0");
            heartLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }

        // 3. Inject both into the Top Bar layout
        if (tokenLabel != null && tokenLabel.getParent() instanceof javafx.scene.layout.Pane parentBox) {

            // Safety check to prevent adding duplicates if initialize() runs twice
            if (!parentBox.getChildren().contains(streakLabel)) {
                int tokenIndex = parentBox.getChildren().indexOf(tokenLabel);

                // Insert Streak at Token's position (pushes Token to the right)
                parentBox.getChildren().add(tokenIndex, streakLabel);

                // Insert Heart right after Streak (pushes Token to the right again)
                parentBox.getChildren().add(tokenIndex + 1, heartLabel);

                // Add nice spacing between them
                if (parentBox instanceof javafx.scene.layout.HBox) {
                    javafx.scene.layout.HBox.setMargin(streakLabel, new javafx.geometry.Insets(0, 15, 0, 0));
                    javafx.scene.layout.HBox.setMargin(heartLabel, new javafx.geometry.Insets(0, 15, 0, 0));
                }
            }
        }
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
            attachWindowFocusRefresh();
        });
    }

    private void attachWindowFocusRefresh() {
        if (contentArea == null || contentArea.getScene() == null) {
            return;
        }
        javafx.stage.Window w = contentArea.getScene().getWindow();
        if (w == null) {
            return;
        }
        if (Boolean.TRUE.equals(w.getProperties().get("codedu.shellFocusRefresh"))) {
            return;
        }
        w.getProperties().put("codedu.shellFocusRefresh", true);
        w.focusedProperty().addListener((obs, was, focused) -> {
            if (Boolean.TRUE.equals(focused)) {
                refreshShellUserFromDatabaseOnWindowFocus();
            }
        });
    }

    private void refreshShellUserFromDatabase() {
        refreshShellUserFromDatabaseInternal(true);
    }

    private void refreshShellUserFromDatabaseOnWindowFocus() {
        refreshShellUserFromDatabaseInternal(false);
    }

    private void refreshShellUserFromDatabaseInternal(boolean force) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!force && now - lastShellUserRefreshMs < FOCUS_REFRESH_MIN_INTERVAL_MS) {
            return;
        }
        userService.getUserWithProfileData(user.getUsername()).ifPresent(fresh -> {
            this.user = fresh;
            if (initDemoModelsIfNeeded()) {
                userService.saveUser(this.user);
            }
            this.gameState = user.getGameState();
            updateHeader();
        });
        lastShellUserRefreshMs = System.currentTimeMillis();
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
        if (welcomeNavLabel != null) {
            welcomeNavLabel.getStyleClass().add(Styles.INTERACTIVE);
            welcomeNavLabel.setOnMouseClicked(e -> loadProfile());
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
        if (this.user == null) return;

        refreshShellUserFromDatabase();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/LearningPath.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();

            LearningPathController lpController = loader.getController();
            // 🚀 Learning Path now emits a ChapterDTO, which we pass to loadChapterView
            lpController.setOnStartChapter(this::loadChapterView);

            lpController.loadUserData(this.user);

            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 🚀 Updated to accept ChapterDTO
    private void loadChapterView(ChapterDTO chapterDto) {
        refreshShellUserFromDatabase();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/ChapterView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent chapterView = loader.load();

            ChapterViewController controller = loader.getController();

            // 1. Create a dummy Chapter entity just to use as a search key for the repository
            Chapter searchKey = new Chapter();
            searchKey.setId(chapterDto.id());

            // 2. See if the user has database progress
            com.codedu.models.learning.UserChapterProgress dbProgress =
                    progressService.getProgress(this.user, searchKey);

            // 3. Map it to the ChapterProgressDTO the UI expects
            ChapterProgressDTO progressDto = new ChapterProgressDTO();
            progressDto.setChapter(chapterDto);
            progressDto.setCompletedLessons(dbProgress != null ? dbProgress.getCompletedLessons() : 0);
            progressDto.setCompleted(dbProgress != null && dbProgress.isCompleted());
            progressDto.setLocked(false);

            controller.setCurrentUser(this.user);

            // 🚀 Pass both DTOs to the ChapterViewController
            controller.setChapter(chapterDto, progressDto);

            controller.setOnProgressUpdated(ignored -> refreshShellUserFromDatabase());

            controller.setOnBack(() -> {
                setActiveButton(btnLearningPath);
                loadLearningPath();
            });

            setContentAndFill(chapterView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Chapter", "Error loading chapter view.");
        }
    }

    private void loadDailyChallenge() {
        refreshShellUserFromDatabase();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/DailyChallenge.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            DailyChallengeController controller = loader.getController();
            // 🚀 Receives standard Question entity, maps it internally
            controller.setOnStartQuestion(this::openChallengePage);
            controller.setOnBack(this::loadLearningPath);
            setContentAndFill(view);
        } catch (Exception ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Daily challenges", "Error loading daily challenges module.");
        }
    }

    // 🚀 Intercepts the raw Entity from Daily Challenge and turns it into a DTO
    private void openChallengePage(Question question) {
        refreshShellUserFromDatabase();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/QuestionSolver.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            QuestionSolverController controller = loader.getController();

            // 🚀 Map the raw Question entity to the QuestionDTO expected by the Solver
            QuestionDTO questionDto = QuestionDTO.builder()
                    .id(question.getId())
                    .title(question.getTitle())
                    .content(question.getContent())
                    .hint(question.getHint())
                    .solution(question.getSolution())
                    .questionType(question.getQuestionType())
                    .questionDifficulty(question.getQuestionDifficulty())
                    .rewardXp(question.getReward() != null ? question.getReward().getXp() : 0)
                    .rewardToken(question.getReward() != null ? question.getReward().getToken() : 0)
                    .boilerplateCode(question instanceof CodeImplementationQuestion cq ? cq.getBoilerplateCode() : "")
                    .build();

            controller.setQuestion(questionDto);

            controller.setOnBack(() -> {
                setActiveButton(btnDailyChallenge);
                loadDailyChallenge();
            });

            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Question Solver", "Error opening question solver.");
        }
    }

    private void loadAchievements() {
        refreshShellUserFromDatabase();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Achievements.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            AchievementsController controller = loader.getController();

            controller.setCurrentUser(this.user);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Achievements", "Error loading achievements module.");
        }
    }

    private void loadForum() {
        refreshShellUserFromDatabase();
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
        refreshShellUserFromDatabase();
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
            if (isSelf) {
                controller.setOnNavigateToInventory(() -> {
                    setActiveButton(btnInventory);
                    loadInventory();
                });
            }
            controller.setUserModel(profileUser);
            controller.setGameState(state);
            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile", "Error loading profile view.");
        }
    }

    private void loadMatchmaking() {
        refreshShellUserFromDatabase();
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
        refreshShellUserFromDatabase();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Store.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            StoreController controller = loader.getController();

            controller.setUserModel(user);

            controller.setOnUserUpdated(updatedUser -> {
                if (updatedUser == null) return;
                refreshShellUserFromDatabase();
            });

            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Store", "Error loading store module.");
        }
    }

    private void loadInventory() {
        refreshShellUserFromDatabase();
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
        refreshShellUserFromDatabase();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/AskAI.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            AIChatbotController controller = loader.getController();
            controller.setCurrentUser(this.user);
            setContentAndFill(view);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Ask AI", "Error loading Ask AI module.");
        }
    }

    private void loadLeaderboard() {
        refreshShellUserFromDatabase();
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
        if (competitor == null || competitor.getUser() == null) {
            return;
        }
        try {
            User hydrated = userService.loadUserForPublicProfile(competitor.getUser().getId())
                    .orElse(competitor.getUser());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Profile.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent profileView = loader.load();
            ProfileController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setViewingSelf(false);
            controller.setOnProfileClick(this::openUserProfile);
            controller.setCompetitor(competitor, competitorOrder, hydrated);
            setContentAndFill(profileView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Profile", "Error loading competitor profile.");
        }
    }

    private void loadSettings() {
        refreshShellUserFromDatabase();
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
        userService.getUserWithProfileData(user.getUsername())
                .ifPresentOrElse(
                        this::openUserProfile,
                        () -> openUserProfile(this.user));
    }

    private UserGameState currentGameStateForHeader() {
        if (user != null && user.getGameState() != null) {
            return user.getGameState();
        }
        return gameState;
    }

    private void updateHeader() {
        String username = (user.getUsername() != null && !user.getUsername().isEmpty()) ? user.getUsername() : "User";
        UserGameState gs = currentGameStateForHeader();
        this.gameState = gs;
        int tokens = (gs != null) ? gs.getTokenBalance() : 0;
        int level = (gs != null) ? gs.getLevel() : 1;
        int xp = (gs != null) ? gs.getXp() : 0;
        int hearts = (gs != null) ? gs.getHeartCount() : 0;
        int streak = (gs != null) ? gs.getCurrentStreak() : 0;

        tokenLabel.setText("Tokens: " + tokens);
        badgeLabel.setText("Lvl " + level);
        welcomeNavLabel.setText("@" + username);

        if (heartLabel != null) {
            heartLabel.setText("\u2764 " + hearts);
        }

        int levelCap = Math.max(1, level * 100);
        double progress = (double) xp / levelCap;
        xpProgressBar.setProgress(Math.min(1.0, progress));
        xpLabel.setText("XP: " + xp + " / " + levelCap);

        if (profileIconLabel != null) {
            profileIconLabel.setText(username.substring(0, 1).toUpperCase());
            profileIconLabel.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: white;");
        }

        if (streakLabel != null) {

            streakLabel.setText("🔥 " + streak);

            if (streak > 0) {
                streakLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            } else {
                streakLabel.setStyle("-fx-text-fill: #95a5a6;"); // Gray if starting out
            }
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

    private boolean initDemoModelsIfNeeded() {
        if (user == null) {
            return false;
        }
        if (userGameStateService != null) {
            userGameStateService.ensureGameStateForUser(user);
            this.gameState = user.getGameState();
        }
        return false;
    }
}
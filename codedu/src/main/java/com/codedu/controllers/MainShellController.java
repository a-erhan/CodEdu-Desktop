package com.codedu.controllers;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Styles;
import com.codedu.models.Chapter;
import com.codedu.models.Competitor;
import com.codedu.models.DailyChallenge;
import com.codedu.models.ForumPost;
import com.codedu.models.LeaderBoard;
import com.codedu.models.User;
import com.codedu.models.UserGameState;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.io.IOException;

/**
 * Controller for the main application shell.
 * Manages sidebar navigation, header data binding, and center content
 * switching.
 */
@Controller
public class MainShellController {

    // --- Header elements ---
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

    // --- Sidebar ---
    @FXML
    private VBox sidebar;
    @FXML
    private Label taglineLabel;

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
    private Button btnMatchmaking;
    @FXML
    private Button btnAskAI;
    @FXML
    private Button btnSettings;

    // --- Center content area ---
    @FXML
    private StackPane contentArea;

    private User user = new User();
    private UserGameState gameState;
    private DailyChallenge todayChallenge;
    private LeaderBoard weeklyLeaderboard;
    private java.util.List<ForumPost> forumThreads = new java.util.ArrayList<>();
    private Button activeButton;

    /**
     * Set the authenticated user from the login screen.
     */
    public void setUser(User user) {
        this.user = user;
        initDemoModelsIfNeeded();
        updateHeader();
    }

    @FXML
    public void initialize() {
        // Bind header to user model
        initDemoModelsIfNeeded();
        updateHeader();

        // Apply Nord typography and container styling
        if (taglineLabel != null) {
            taglineLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (sidebar != null) {
            sidebar.setSpacing(16);
            sidebar.setFillWidth(true);
            sidebar.getStyleClass().addAll(Styles.BG_SUBTLE, Styles.ELEVATED_1, Styles.ROUNDED);
        }
        if (welcomeNavLabel != null) {
            welcomeNavLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        }

        // Apply common Nord nav-button styling
        styleNavButton(btnLearningPath);
        styleNavButton(btnDailyChallenge);
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
            showDailyChallenge();
        });

        // Leaderboard
        setupNavButtonWithHover(btnLeaderboard);
        btnLeaderboard.setOnAction(e -> {
            setActiveButton(btnLeaderboard);
            showLeaderboard();
        });

        // Forum
        setupNavButtonWithHover(btnForum);
        btnForum.setOnAction(e -> {
            setActiveButton(btnForum);
            showForum();
        });

        // Matchmaking
        setupNavButtonWithHover(btnMatchmaking);
        btnMatchmaking.setOnAction(e -> {
            setActiveButton(btnMatchmaking);
            showMatchmaking();
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
            showAskAI();
        });

        // Settings — loads full FXML module
        setupNavButtonWithHover(btnSettings);
        btnSettings.setOnAction(e -> {
            setActiveButton(btnSettings);
            loadSettings();
        });

        // Default to Journey (Learning Path) instead of welcome hero page
        setActiveButton(btnLearningPath);
        loadLearningPath();
    }

    private void updateHeader() {
        String username = user.getUsername() != null ? user.getUsername() : "User";
        badgeLabel.setText("");
        tokenLabel.setText("Tokens: " + user.getTokenBalance());

        if (welcomeNavLabel != null) {
            welcomeNavLabel.setText("Welcome @" + username);
            welcomeNavLabel.setOnMouseClicked(e -> loadProfile());
        }

        int level = gameState != null ? gameState.getLevel() : 1;
        int xp = gameState != null ? gameState.getXp() : 0;
        int levelCap = Math.max(1, level * 1000);
        double progress = Math.max(0, Math.min(1, (double) xp / levelCap));
        xpProgressBar.setProgress(progress);
        xpLabel.setText("XP: " + xp + " / " + levelCap);
    }

    private void initDemoModelsIfNeeded() {
        if (gameState == null) {
            gameState = UserGameState.builder()
                    .user(user)
                    .level(1)
                    .xp(0)
                    .heartCount(3)
                    .build();
        }

        if (todayChallenge == null) {
            todayChallenge = DailyChallenge.builder()
                    .name("Loops & counters")
                    .description("Write a function that prints the numbers from 1 to 100 and counts how many are even.")
                    .xpRewards(50)
                    .tokenRewards(25)
                    .build();
        }

        if (weeklyLeaderboard == null) {
            // Create mock competitors, assigning one to the current user
            Competitor me = Competitor.builder()
                    .user(user)
                    .rankingPoint(2180)
                    .totalWins(12)
                    .totalLosses(8)
                    .totalMatches(20)
                    .build();
            Competitor c1 = Competitor.builder().rankingPoint(2840).totalWins(20).totalLosses(5).totalMatches(25).build();
            Competitor c2 = Competitor.builder().rankingPoint(2650).totalWins(18).totalLosses(6).totalMatches(24).build();
            Competitor c3 = Competitor.builder().rankingPoint(2420).totalWins(15).totalLosses(7).totalMatches(22).build();
            Competitor c5 = Competitor.builder().rankingPoint(1950).totalWins(10).totalLosses(9).totalMatches(19).build();

            weeklyLeaderboard = LeaderBoard.builder()
                    .name("Weekly XP")
                    .userRank(4) // 4th place
                    .requiredLevel(1)
                    .build();
            weeklyLeaderboard.addCompetitor(c1);
            weeklyLeaderboard.addCompetitor(c2);
            weeklyLeaderboard.addCompetitor(c3);
            weeklyLeaderboard.addCompetitor(me);
            weeklyLeaderboard.addCompetitor(c5);
        }

        if (forumThreads.isEmpty()) {
            String authorName = user.getUsername() != null ? user.getUsername() : "You";
            User author = User.builder()
                    .username(authorName)
                    .email(authorName.toLowerCase() + "@example.com")
                    .password("")
                    .build();

            forumThreads.add(ForumPost.builder()
                    .title("How do I fix a NullPointerException in Java?")
                    .content("I am looping over a list and sometimes get a NullPointerException. How can I debug this?")
                    .author(author)
                    .build());
            forumThreads.add(ForumPost.builder()
                    .title("Best way to understand loops as a beginner?")
                    .content("I get confused with indices in for-loops. Any mental models that helped you?")
                    .author(author)
                    .build());
            forumThreads.add(ForumPost.builder()
                    .title("Share your favorite resources for learning SQL")
                    .content("Looking for interactive resources that teach SQL with real-world examples.")
                    .author(author)
                    .build());
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

    private void setupNavButton(Button button, String sectionTitle, String sectionDescription) {
        setupNavButtonWithHover(button);
        button.setOnAction(e -> {
            setActiveButton(button);
            showSectionPlaceholder(sectionTitle, sectionDescription);
        });
    }

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
            contentArea.getChildren().setAll(chapterView);
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
            contentArea.getChildren().setAll(storeView);
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
            controller.setUserModel(user);
            controller.setGameState(gameState);
            contentArea.getChildren().setAll(profileView);
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
            contentArea.getChildren().setAll(settingsView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showSectionPlaceholder("Settings",
                    "Error loading settings module: " + ex.getMessage());
        }
    }

    // Track theme explicitly instead of inspecting the UA string
    private boolean darkTheme = true;

    private void toggleTheme() {
        darkTheme = !darkTheme;
        if (darkTheme) {
            Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
        }
    }

    // --- Content helpers ---

    private void showWelcome() {
        VBox root = new VBox(24);
        root.setAlignment(Pos.CENTER);
        root.setMaxWidth(900);

        String username = user.getUsername() != null ? user.getUsername() : "User";

        Label welcome = new Label("Welcome " + username);
        welcome.getStyleClass().add(Styles.TITLE_3);

        // --- Hero section ---
        VBox heroBox = new VBox(12);
        heroBox.setAlignment(Pos.CENTER);

        Label tagline = new Label("CODE MAKES PERFECT");
        tagline.getStyleClass().add("hero-tagline");

        Label title = new Label("The free, fun, and effective way to learn to code!");
        title.getStyleClass().addAll("welcome-title", "hero-main-title");
        title.setWrapText(true);

        Label subtitle = new Label(
                "Write real code from day one, track your streak, and compete with other learners \u2014 all inside CodEdu.");
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setWrapText(true);

        Label metric = new Label("Join over 3,165,792 codders");
        metric.getStyleClass().addAll("welcome-stats", "hero-metric");

        HBox ctaRow = new HBox(12);
        ctaRow.setAlignment(Pos.CENTER);

        Button btnGetStarted = new Button("GET STARTED");
        btnGetStarted.getStyleClass().add("hero-cta-primary");
        btnGetStarted.setOnAction(e -> {
            setActiveButton(btnLearningPath);
            loadLearningPath();
        });

        Button btnContinue = new Button("CONTINUE WHERE I LEFT OFF");
        btnContinue.getStyleClass().add("hero-cta-secondary");
        btnContinue.setOnAction(e -> {
            setActiveButton(btnAskAI);
            loadProfile();
        });

        ctaRow.getChildren().addAll(btnGetStarted, btnContinue);

        HBox languagesRow = new HBox(8);
        languagesRow.setAlignment(Pos.CENTER);

        String[] languages = {"Python", "HTML", "JavaScript", "Java", "C++", "SQL", "C", "CSS"};
        for (int i = 0; i < languages.length; i++) {
            Label pill = new Label(languages[i]);
            pill.getStyleClass().add("hero-language-pill");
            if (i == 0) {
                pill.getStyleClass().add("hero-language-pill-featured");
            }
            languagesRow.getChildren().add(pill);
        }

        heroBox.getChildren().addAll(tagline, title, subtitle, metric, ctaRow, languagesRow);

        // --- Feature cards row (mirrors Coddy sections) ---
        HBox sectionsRow = new HBox(16);
        sectionsRow.setAlignment(Pos.CENTER);
        sectionsRow.getStyleClass().add("hero-section-row");

        VBox learnByDoing = new VBox(8);
        learnByDoing.setAlignment(Pos.TOP_LEFT);
        learnByDoing.getStyleClass().add("hero-section-card");
        learnByDoing.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));

        Label learnTitle = new Label("Learn by Doing");
        learnTitle.getStyleClass().add("hero-section-title");
        Label learnBody = new Label(
                "Solve bite-sized interactive challenges and see instant feedback in the built-in code editor.");
        learnBody.getStyleClass().add("hero-section-body");
        learnBody.setWrapText(true);
        Label learnBadge = new Label("PLAYGROUND & TEST CASES");
        learnBadge.getStyleClass().add("hero-section-badge");
        learnByDoing.getChildren().addAll(learnTitle, learnBody, learnBadge);

        VBox streakCard = new VBox(8);
        streakCard.setAlignment(Pos.TOP_LEFT);
        streakCard.getStyleClass().add("hero-section-card");
        streakCard.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));

        Label streakTitle = new Label("Build Your Coding Streak");
        streakTitle.getStyleClass().add("hero-section-title");
        Label streakBody = new Label(
                "Keep your daily habit alive with streaks, freeze days, and friendly competition on leaderboards.");
        streakBody.getStyleClass().add("hero-section-body");
        streakBody.setWrapText(true);
        Label streakBadge = new Label("DAILY CHALLENGES");
        streakBadge.getStyleClass().add("hero-section-badge");
        streakCard.getChildren().addAll(streakTitle, streakBody, streakBadge);

        VBox proveCard = new VBox(8);
        proveCard.setAlignment(Pos.TOP_LEFT);
        proveCard.getStyleClass().add("hero-section-card");
        proveCard.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));

        Label proveTitle = new Label("Prove Your Skills");
        proveTitle.getStyleClass().add("hero-section-title");
        Label proveBody = new Label(
                "Earn achievements and certificates as you complete learning paths and master new concepts.");
        proveBody.getStyleClass().add("hero-section-body");
        proveBody.setWrapText(true);
        Label proveBadge = new Label("CERTIFICATES & BADGES");
        proveBadge.getStyleClass().add("hero-section-badge");
        proveCard.getChildren().addAll(proveTitle, proveBody, proveBadge);

        sectionsRow.getChildren().addAll(learnByDoing, streakCard, proveCard);

        root.getChildren().addAll(welcome, heroBox, sectionsRow);
        contentArea.getChildren().setAll(root);
    }

    private void showDailyChallenge() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(900);

        Label title = new Label("Daily Challenges");
        title.getStyleClass().add(Styles.TITLE_3);

        Label subtitle = new Label(
                "Tackle today’s challenges. Each card is a separate task with its own rewards.");
        subtitle.setWrapText(true);

        VBox list = new VBox(16);
        list.setFillWidth(true);

        // For now, create a small list of mock challenges using the DailyChallenge model
        java.util.List<DailyChallenge> challenges = java.util.List.of(
                todayChallenge,
                DailyChallenge.builder()
                        .name("Array Practice")
                        .description("Work with arrays: sum numbers, find max, and reverse the list.")
                        .xpRewards(40)
                        .tokenRewards(15)
                        .build(),
                DailyChallenge.builder()
                        .name("Debug the Loop")
                        .description("Fix an off-by-one error in a for-loop and make tests pass.")
                        .xpRewards(35)
                        .tokenRewards(10)
                        .build()
        );

        for (DailyChallenge ch : challenges) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            Label chTitle = new Label(ch.getName());
            chTitle.getStyleClass().add(Styles.TEXT_BOLD);

            Label chBody = new Label(ch.getDescription());
            chBody.setWrapText(true);

            Label chMeta = new Label(ch.getXpRewards() + " XP · " + ch.getTokenRewards() + " tokens");
            chMeta.getStyleClass().add(Styles.TEXT_SUBTLE);

            card.getChildren().addAll(chTitle, chBody, chMeta);
            list.getChildren().add(card);
        }

        root.getChildren().addAll(title, subtitle, list);
        contentArea.getChildren().setAll(root);
    }

    private void showLeaderboard() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(900);

        Label title = new Label("Achievements");
        title.getStyleClass().add(Styles.TITLE_3);

        Label subtitle = new Label(
                "Celebrate your progress with medium‑term learning achievements.");
        subtitle.setWrapText(true);

        VBox list = new VBox(16);
        list.setFillWidth(true);

        // Represent goals using competitors data for now
        java.util.List<Competitor> competitors = weeklyLeaderboard.getCompetitors();
        for (int i = 0; i < competitors.size(); i++) {
            Competitor c = competitors.get(i);
            VBox goalCard = new VBox(6);
            goalCard.setAlignment(Pos.TOP_LEFT);
            goalCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            goalCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            String nameText = c.getUser() != null && c.getUser().getUsername() != null
                    ? c.getUser().getUsername()
                    : "Goal " + (i + 1);

            Label goalTitle = new Label(nameText);
            goalTitle.getStyleClass().add(Styles.TEXT_BOLD);

            Label goalBody = new Label("Reach " + c.getRankingPoint() + " XP with at least "
                    + String.format("%.0f", c.getWinRate()) + "% win rate.");
            goalBody.setWrapText(true);

            Label goalMeta = new Label("Progress goal");
            goalMeta.getStyleClass().add(Styles.TEXT_SUBTLE);

            goalCard.getChildren().addAll(goalTitle, goalBody, goalMeta);
            list.getChildren().add(goalCard);
        }

        root.getChildren().addAll(title, subtitle, list);
        contentArea.getChildren().setAll(root);
    }

    private void showForum() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(900);

        Label title = new Label("Forum");
        title.getStyleClass().add(Styles.TITLE_3);

        Label subtitle = new Label(
                "Ask questions, share tips, and read what other learners are working on.");
        subtitle.setWrapText(true);

        VBox list = new VBox(16);
        list.setFillWidth(true);

        for (ForumPost post : forumThreads) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            Label postTitle = new Label(post.getTitle());
            postTitle.getStyleClass().add(Styles.TEXT_BOLD);

            String authorName = (post.getAuthor() != null && post.getAuthor().getUsername() != null)
                    ? post.getAuthor().getUsername()
                    : "Anonymous";
            Label meta = new Label("Posted by " + authorName);
            meta.getStyleClass().add(Styles.TEXT_SUBTLE);

            Label content = new Label(post.getContent());
            content.setWrapText(true);

            card.getChildren().addAll(postTitle, meta, content);
            list.getChildren().add(card);
        }

        root.getChildren().addAll(title, subtitle, list);
        contentArea.getChildren().setAll(root);
    }

    private void showMatchmaking() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(1100);

        // Header row with title on the left and time on the right
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Matchmaking");
        title.getStyleClass().add(Styles.TITLE_3);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label("Time: 00:00");
        timeLabel.getStyleClass().add(Styles.TEXT_SUBTLE);

        headerRow.getChildren().addAll(title, spacer, timeLabel);

        Label subtitle = new Label("Solve the coding challenge faster and more accurately than your opponent to win!");
        subtitle.setWrapText(true);

        // Main content row: left = challenge + editor, right = score panel
        HBox mainRow = new HBox(16);
        mainRow.setAlignment(Pos.TOP_LEFT);

        VBox leftColumn = new VBox(12);
        leftColumn.setPrefWidth(750);

        // Status card
        VBox statusCard = new VBox(6);
        statusCard.setAlignment(Pos.TOP_LEFT);
        statusCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
        statusCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        HBox statusRow = new HBox(6);
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add(Styles.TEXT_BOLD);
        Label statusValue = new Label("Waiting for opponent...");
        statusValue.getStyleClass().add(Styles.TEXT_SUBTLE);
        statusRow.getChildren().addAll(statusLabel, statusValue);

        statusCard.getChildren().add(statusRow);

        // Challenge card
        VBox challengeCard = new VBox(6);
        challengeCard.setAlignment(Pos.TOP_LEFT);
        challengeCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
        challengeCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        Label challengeHeader = new Label("Challenge");
        challengeHeader.getStyleClass().add(Styles.TEXT_BOLD);

        Label problemTitle = new Label("Problem title");
        problemTitle.getStyleClass().add(Styles.TEXT_SUBTLE);

        Label problemDescription = new Label("Problem description will appear here when a match starts.");
        problemDescription.setWrapText(true);

        challengeCard.getChildren().addAll(challengeHeader, problemTitle, problemDescription);

        // Code editor + actions + output
        VBox editorCard = new VBox(8);
        editorCard.setAlignment(Pos.TOP_LEFT);
        editorCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
        editorCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        Label editorLabel = new Label("Code editor");
        editorLabel.getStyleClass().add(Styles.TEXT_BOLD);

        TextArea codeArea = new TextArea();
        codeArea.setPromptText("Write your solution here...");
        codeArea.setPrefRowCount(10);

        HBox actionRow = new HBox(8);
        Button runButton = new Button("Run code");
        runButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
        runButton.setDisable(true); // prototype

        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
        submitButton.setDisable(true); // prototype
        actionRow.getChildren().addAll(runButton, submitButton);

        Label outputLabel = new Label("Output");
        outputLabel.getStyleClass().add(Styles.TEXT_BOLD);

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(4);

        editorCard.getChildren().addAll(editorLabel, codeArea, actionRow, outputLabel, outputArea);

        leftColumn.getChildren().addAll(statusCard, challengeCard, editorCard);

        // Score / attempts side panel
        VBox scoreCard = new VBox(10);
        scoreCard.setAlignment(Pos.TOP_LEFT);
        scoreCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
        scoreCard.setPrefWidth(220);
        scoreCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        Label scoreTitle = new Label("Score");
        scoreTitle.getStyleClass().add(Styles.TEXT_BOLD);

        Label youScore = new Label("You: 0");
        Label opponentScore = new Label("Opponent: 0");

        Label attemptsTitle = new Label("Attempts");
        attemptsTitle.getStyleClass().add(Styles.TEXT_BOLD);

        Label youAttempts = new Label("You: 0");
        Label opponentAttempts = new Label("Opponent: 0");

        youScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        opponentScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        youAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);
        opponentAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);

        scoreCard.getChildren().addAll(
                scoreTitle,
                youScore,
                opponentScore,
                attemptsTitle,
                youAttempts,
                opponentAttempts
        );

        mainRow.getChildren().addAll(leftColumn, scoreCard);

        root.getChildren().addAll(headerRow, subtitle, mainRow);
        contentArea.getChildren().setAll(root);
    }

    private void showAskAI() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(800);

        Label title = new Label("Ask AI tutor");
        title.getStyleClass().add(Styles.TITLE_3);

        Label subtitle = new Label(
                "Type a question about your code or a concept you are learning. In a full version, an AI tutor would answer here.");
        subtitle.setWrapText(true);

        VBox card = new VBox(12);
        card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));

        Label promptLabel = new Label("Your question");
        promptLabel.getStyleClass().add(Styles.TEXT_BOLD);

        TextArea area = new TextArea();
        area.setPromptText("For example: \"Why am I getting an index out of bounds error in this loop?\"");
        area.setPrefRowCount(5);

        Label note = new Label("Responses are not yet wired up in this prototype, but this is where explanations would appear.");
        note.getStyleClass().add(Styles.TEXT_SUBTLE);
        note.setWrapText(true);

        card.getChildren().addAll(promptLabel, area, note);

        root.getChildren().addAll(title, subtitle, card);
        contentArea.getChildren().setAll(root);
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

        Label comingSoon = new Label("Module coming soon\u2026");
        comingSoon.getStyleClass().add("coming-soon");

        box.getChildren().addAll(titleLabel, descLabel, comingSoon);
        contentArea.getChildren().setAll(box);
    }
}
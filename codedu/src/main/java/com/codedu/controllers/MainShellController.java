package com.codedu.controllers;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
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
        String username = user.getUsername() != null ? user.getUsername() : "User";
        badgeLabel.setText("");
        usernameLabel.setText(username);
        tokenLabel.setText("Tokens: " + user.getTokenBalance());

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
            Competitor c1 = Competitor.builder().rankingPoint(2840).totalWins(20).totalLosses(5).totalMatches(25).build();
            Competitor c2 = Competitor.builder().rankingPoint(2650).totalWins(18).totalLosses(6).totalMatches(24).build();
            Competitor c3 = Competitor.builder().rankingPoint(2420).totalWins(15).totalLosses(7).totalMatches(22).build();
            Competitor c4 = Competitor.builder().rankingPoint(2180).totalWins(12).totalLosses(8).totalMatches(20).build();
            Competitor c5 = Competitor.builder().rankingPoint(1950).totalWins(10).totalLosses(9).totalMatches(19).build();

            weeklyLeaderboard = LeaderBoard.builder()
                    .name("Weekly XP")
                    .userRank(4)
                    .requiredLevel(1)
                    .build();
            weeklyLeaderboard.addCompetitor(c1);
            weeklyLeaderboard.addCompetitor(c2);
            weeklyLeaderboard.addCompetitor(c3);
            weeklyLeaderboard.addCompetitor(c4);
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

    private void toggleTheme() {
        // Toggle only the AtlantaFX user-agent stylesheet between Nord light and dark.
        String current = Application.getUserAgentStylesheet();
        if (current != null && current.contains("NordDark")) {
            Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        }
    }

    // --- Content helpers ---

    private void showWelcome() {
        VBox root = new VBox(24);
        root.setAlignment(Pos.CENTER);
        root.setMaxWidth(900);

        // --- Hero section (Coddy-style landing) ---
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
            setActiveButton(btnProfile);
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

        root.getChildren().addAll(heroBox, sectionsRow);
        contentArea.getChildren().setAll(root);
    }

    private void showDailyChallenge() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(900);

        Label title = new Label("Daily challenge");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(
                "Solve a fresh coding task every day and keep your streak alive.");
        subtitle.getStyleClass().add("section-description");
        subtitle.setWrapText(true);

        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_CENTER);

        VBox challengeCard = new VBox(10);
        challengeCard.getStyleClass().add("hero-section-card");
        challengeCard.setAlignment(Pos.TOP_LEFT);
        challengeCard.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));
        Label chTitle = new Label("Today’s task: " + todayChallenge.getName());
        chTitle.getStyleClass().add("hero-section-title");
        Label chBody = new Label(todayChallenge.getDescription());
        chBody.getStyleClass().add("hero-section-body");
        chBody.setWrapText(true);
        Label chBadge = new Label(todayChallenge.getXpRewards() + " XP · " + todayChallenge.getTokenRewards() + " tokens");
        chBadge.getStyleClass().add("hero-section-badge");
        challengeCard.getChildren().addAll(chTitle, chBody, chBadge);

        VBox streakCard = new VBox(10);
        streakCard.getStyleClass().add("hero-section-card");
        streakCard.setAlignment(Pos.TOP_LEFT);
        streakCard.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));
        Label stTitle = new Label("Your streak");
        stTitle.getStyleClass().add("hero-section-title");
        Label stBody = new Label("You have a 0-day streak. Complete today’s challenge to start building momentum.");
        stBody.getStyleClass().add("hero-section-body");
        stBody.setWrapText(true);
        Label stBadge = new Label("STREAKS & FREEZE DAYS");
        stBadge.getStyleClass().add("hero-section-badge");
        streakCard.getChildren().addAll(stTitle, stBody, stBadge);

        row.getChildren().addAll(challengeCard, streakCard);

        root.getChildren().addAll(title, subtitle, row);
        contentArea.getChildren().setAll(root);
    }

    private void showLeaderboard() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(700);

        Label title = new Label("Leaderboard");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(
                "See how you rank against other learners this week.");
        subtitle.getStyleClass().add("section-description");
        subtitle.setWrapText(true);

        VBox board = new VBox(8);
        board.getStyleClass().add("hero-section-card");
        board.setPadding(new javafx.geometry.Insets(16, 20, 16, 20));

        java.util.List<Competitor> competitors = weeklyLeaderboard.getCompetitors();
        for (int i = 0; i < competitors.size(); i++) {
            Competitor c = competitors.get(i);
            HBox line = new HBox(12);
            line.setAlignment(Pos.CENTER_LEFT);

            Label pos = new Label(String.valueOf(i + 1));
            pos.getStyleClass().add("hero-section-badge");

            String nameText = c.getUser() != null && c.getUser().getUsername() != null
                    ? c.getUser().getUsername()
                    : "Player " + (i + 1);
            Label name = new Label(nameText);
            name.getStyleClass().add("hero-section-title");

            Label score = new Label(c.getRankingPoint() + " XP · " + String.format("%.0f", c.getWinRate()) + "% win rate");
            score.getStyleClass().add("hero-section-body");

            line.getChildren().addAll(pos, name, score);
            board.getChildren().add(line);
        }

        root.getChildren().addAll(title, subtitle, board);
        contentArea.getChildren().setAll(root);
    }

    private void showForum() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(900);

        Label title = new Label("Community discussions");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(
                "Ask questions, share solutions, and help other learners.");
        subtitle.getStyleClass().add("section-description");
        subtitle.setWrapText(true);

        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_CENTER);

        VBox threads = new VBox(10);
        threads.getStyleClass().add("hero-section-card");
        threads.setAlignment(Pos.TOP_LEFT);
        threads.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));
        Label thTitle = new Label("Trending threads");
        thTitle.getStyleClass().add("hero-section-title");
        threads.getChildren().add(thTitle);
        for (ForumPost post : forumThreads) {
            String authorName = post.getAuthor() != null && post.getAuthor().getUsername() != null
                    ? post.getAuthor().getUsername()
                    : "Anonymous";
            Label line = new Label("• " + post.getTitle() + " — " + authorName);
            line.getStyleClass().add("hero-section-body");
            threads.getChildren().add(line);
        }

        VBox tips = new VBox(10);
        tips.getStyleClass().add("hero-section-card");
        tips.setAlignment(Pos.TOP_LEFT);
        tips.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));
        Label tipsTitle = new Label("Posting guidelines");
        tipsTitle.getStyleClass().add("hero-section-title");
        Label tipsBody = new Label(
                "Describe your problem clearly, include code snippets, and share what you have tried so far.");
        tipsBody.getStyleClass().add("hero-section-body");
        tipsBody.setWrapText(true);
        Label tipsBadge = new Label("HELP OTHERS LEARN");
        tipsBadge.getStyleClass().add("hero-section-badge");
        tips.getChildren().addAll(tipsTitle, tipsBody, tipsBadge);

        row.getChildren().addAll(threads, tips);

        root.getChildren().addAll(title, subtitle, row);
        contentArea.getChildren().setAll(root);
    }

    private void showAskAI() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setMaxWidth(800);

        Label title = new Label("Ask the tutor");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(
                "Type a question about your code or a concept you are learning. In a full version, an AI tutor would answer here.");
        subtitle.getStyleClass().add("section-description");
        subtitle.setWrapText(true);

        VBox card = new VBox(12);
        card.getStyleClass().add("hero-section-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));

        Label promptLabel = new Label("Your question");
        promptLabel.getStyleClass().add("hero-section-title");

        TextArea area = new TextArea();
        area.setPromptText("For example: \"Why am I getting an index out of bounds error in this loop?\"");
        area.setPrefRowCount(5);

        Label note = new Label("Responses are not yet wired up in this prototype, but this is where explanations would appear.");
        note.getStyleClass().add("hero-section-body");
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
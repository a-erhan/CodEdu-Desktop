package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.DailyChallenge;
import com.codedu.services.CodeExecutionService;
import com.codedu.services.MatchmakingService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.codedu.services.DailyChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MatchmakingController {

    // --- UI Elements ---
    @FXML
    private Label titleLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox statusCard;
    @FXML
    private Label statusLabel;
    @FXML
    private Label statusValue;

    @FXML
    private VBox challengeCard;
    @FXML
    private Label challengeHeader;
    @FXML
    private Label problemTitle;
    @FXML
    private Label problemDescription;

    @FXML
    private VBox editorCard;
    @FXML
    private Label editorLabel;
    @FXML
    private TextArea codeArea;
    @FXML
    private HBox actionRow;
    @FXML
    private Button runButton;
    @FXML
    private Button submitButton;
    @FXML
    private Label outputLabel;
    @FXML
    private TextArea outputArea;

    @FXML
    private VBox scoreCard;
    @FXML
    private Label scoreTitle;
    @FXML
    private Label youScore;
    @FXML
    private Label opponentScore;
    @FXML
    private Label attemptsTitle;
    @FXML
    private Label youAttempts;
    @FXML
    private Label opponentAttempts;

    private DailyChallenge activeChallenge;

    // --- Backend Services ---
    private final CodeExecutionService codeExecutionService;
    private final MatchmakingService matchmakingService;
    private final DailyChallengeService dailyChallengeService;

    // Spring Boot will automatically inject our services here
    @Autowired
    public MatchmakingController(CodeExecutionService codeExecutionService,
            MatchmakingService matchmakingService,
            DailyChallengeService dailyChallengeService) {
        this.codeExecutionService = codeExecutionService;
        this.matchmakingService = matchmakingService;
        this.dailyChallengeService = dailyChallengeService;
    }

    public void setChallenge(DailyChallenge challenge) {
        this.activeChallenge = challenge;
        applyChallenge();
    }

    @FXML
    public void initialize() {
        // ... (Your existing styling code remains exactly the same) ...
        if (titleLabel != null)
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        if (statusCard != null) {
            statusCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            statusCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (statusLabel != null)
            statusLabel.getStyleClass().add(Styles.TEXT_BOLD);
        if (statusValue != null)
            statusValue.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (challengeCard != null) {
            challengeCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            challengeCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (challengeHeader != null)
            challengeHeader.getStyleClass().add(Styles.TEXT_BOLD);
        if (problemTitle != null)
            problemTitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (editorCard != null) {
            editorCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            editorCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (editorLabel != null)
            editorLabel.getStyleClass().add(Styles.TEXT_BOLD);

        // Button Styling and Actions
        if (runButton != null) {
            runButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            // CONNECTING THE RUN BUTTON TO OUR BACKEND
            runButton.setOnAction(e -> handleRunCode());
        }

        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            submitButton.setOnAction(e -> handleSubmitCode());
        }

        if (outputLabel != null)
            outputLabel.getStyleClass().add(Styles.TEXT_BOLD);
        if (scoreCard != null) {
            scoreCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            scoreCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (scoreTitle != null)
            scoreTitle.getStyleClass().add(Styles.TEXT_BOLD);
        if (youScore != null)
            youScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (opponentScore != null)
            opponentScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (attemptsTitle != null)
            attemptsTitle.getStyleClass().add(Styles.TEXT_BOLD);
        if (youAttempts != null)
            youAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (opponentAttempts != null)
            opponentAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);

        applyChallenge();

        // Simulate Matchmaking process
        if (statusValue != null)
            statusValue.setText("Finding an opponent...");
        long randomPlayerId = (long) (Math.random() * 10000);
        matchmakingService.joinQueue(randomPlayerId);

        Task<Void> matchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(2000); // simulate 2 seconds waiting
                return null;
            }
        };
        matchTask.setOnSucceeded(e -> {
            if (statusValue != null)
                statusValue.setText("Match Started!");
            DailyChallenge mockChallenge = new DailyChallenge();
            mockChallenge.setName("Two Sum");
            mockChallenge.setDescription(
                    "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. Write your solution in the code editor.");
            setChallenge(mockChallenge);
            if (runButton != null)
                runButton.setDisable(false);
            if (submitButton != null)
                submitButton.setDisable(false);
        });
        new Thread(matchTask).start();
    }

    private void applyChallenge() {
        if (activeChallenge == null || problemTitle == null || problemDescription == null) {
            return;
        }
        problemTitle.setText(activeChallenge.getName());
        problemDescription.setText(activeChallenge.getDescription());
    }

    /**
     * Triggered when the user clicks the "Run" button.
     * Uses a background Task so the JavaFX UI doesn't freeze while waiting for
     * Judge0.
     */
    private void handleRunCode() {
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            outputArea.setText("Please write some code first!");
            return;
        }

        outputArea.setText("Sending code to Judge0 for execution...\n");
        runButton.setDisable(true); // Prevent spam clicking

        // JavaFX Background Task
        Task<String> executionTask = new Task<>() {
            @Override
            protected String call() {
                // Passing empty string for input since it's just a general run
                return codeExecutionService.executeJavaCode(code, "");
            }
        };

        executionTask.setOnSucceeded(event -> {
            outputArea.setText(executionTask.getValue());
            runButton.setDisable(false);
        });

        executionTask.setOnFailed(event -> {
            outputArea.setText("An error occurred during execution.");
            runButton.setDisable(false);
        });

        new Thread(executionTask).start();
    }

    /**
     * Triggered when the user clicks "Submit".
     */
    private void handleSubmitCode() {
        outputArea.setText("Evaluating against test cases...");
        // We will implement the full validation logic here later
    }
}
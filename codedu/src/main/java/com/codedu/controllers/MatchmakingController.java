package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.matchmaking.GameRoom;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.CodeExecutionService;
import com.codedu.services.interfaces.QuestionEvaluationService;
import com.codedu.services.interfaces.WebSocketClientService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controls the Matchmaking view.
 *
 * <p>
 * Two UI states:
 * <ol>
 * <li><b>Lobby</b> — "Find Match" button visible, match pane hidden.</li>
 * <li><b>Match Active</b> — lobby hidden, question + editor + timer shown.</li>
 * </ol>
 */
@Controller
public class MatchmakingController {

    // ── Lobby FXML ──────────────────────────────────────────────────────
    @FXML
    private VBox lobbyPane;
    @FXML
    private Label lobbyTitle;
    @FXML
    private Label lobbySubtitle;
    @FXML
    private Button findMatchButton;
    @FXML
    private Label lobbyStatus;

    // ── Match FXML ──────────────────────────────────────────────────────
    @FXML
    private VBox matchPane;
    @FXML
    private Label titleLabel;
    @FXML
    private Label timerLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox statusCard;
    @FXML
    private Label statusValue;
    @FXML
    private VBox challengeCard;
    @FXML
    private Label problemTitle;
    @FXML
    private Label problemDescription;
    @FXML
    private VBox editorCard;
    @FXML
    private TextArea codeArea;
    @FXML
    private Button runButton;
    @FXML
    private Button submitButton;
    @FXML
    private TextArea outputArea;
    @FXML
    private VBox scoreCard;
    @FXML
    private Label youScore;
    @FXML
    private Label opponentScore;
    @FXML
    private Label youAttempts;
    @FXML
    private Label opponentAttempts;

    // ── State ───────────────────────────────────────────────────────────
    private User currentUser;
    private CodeImplementationQuestion activeQuestion;
    private int elapsedSeconds = 0;
    private Timeline matchTimer;

    // ── Services ────────────────────────────────────────────────────────
    private final CodeExecutionService codeExecutionService;
    private final QuestionEvaluationService evaluationService;
    private final com.codedu.repositories.interfaces.QuestionRepository questionRepository;
    private final WebSocketClientService webSocketClientService;

    @Autowired
    public MatchmakingController(CodeExecutionService codeExecutionService,
            QuestionEvaluationService evaluationService,
            com.codedu.repositories.interfaces.QuestionRepository questionRepository,
            WebSocketClientService webSocketClientService) {
        this.codeExecutionService = codeExecutionService;
        this.evaluationService = evaluationService;
        this.questionRepository = questionRepository;
        this.webSocketClientService = webSocketClientService;
    }

    // ====================================================================
    // Lifecycle
    // ====================================================================

    @FXML
    public void initialize() {
        showLobby();
        applyLobbyStyles();
        wireButtons();
    }

    /**
     * Called by MainShellController after FXML loading.
     * Stores the current user but does NOT auto-join the queue.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // ====================================================================
    // Lobby → Find Match
    // ====================================================================

    private void onFindMatchClicked() {
        if (currentUser == null)
            return;

        findMatchButton.setDisable(true);
        lobbyStatus.setText("Searching for opponent...");
        System.out.println("[MC] Find Match clicked. Connecting to matchmaking...");

        webSocketClientService.connectAndJoinMatchmaking(
                currentUser.getId(), this::onMatchFound);
    }

    // ====================================================================
    // Match found callback (runs on STOMP thread)
    // ====================================================================

    private void onMatchFound(GameRoom gameRoom) {
        System.out.println("[MC] >>> onMatchFound! Room: " + gameRoom.getRoomId());

        User opponent = (gameRoom.getPlayer1().getId() == currentUser.getId())
                ? gameRoom.getPlayer2()
                : gameRoom.getPlayer1();

        // Reload full question from DB (testCases are @JsonIgnored on the wire).
        CodeImplementationQuestion fullQuestion = null;
        if (gameRoom.getQuestion() != null) {
            try {
                fullQuestion = (CodeImplementationQuestion) questionRepository
                        .findById(gameRoom.getQuestion().getId())
                        .orElse(gameRoom.getQuestion());
            } catch (Exception e) {
                System.err.println("[MC] Error reloading question: " + e.getMessage());
                fullQuestion = gameRoom.getQuestion();
            }
        }

        final CodeImplementationQuestion question = fullQuestion;
        final String opponentName = opponent != null ? opponent.getUsername() : "Opponent";

        Platform.runLater(() -> showMatch(opponentName, question));
    }

    // ====================================================================
    // UI State Transitions
    // ====================================================================

    private void showLobby() {
        if (lobbyPane != null)
            lobbyPane.setVisible(true);
        if (matchPane != null)
            matchPane.setVisible(false);
        if (findMatchButton != null)
            findMatchButton.setDisable(false);
        if (lobbyStatus != null)
            lobbyStatus.setText("");
    }

    private void showMatch(String opponentName, CodeImplementationQuestion question) {
        System.out.println("[MC] >>> showMatch on FX thread. Opponent: " + opponentName);

        // Swap panes
        if (lobbyPane != null)
            lobbyPane.setVisible(false);
        if (matchPane != null)
            matchPane.setVisible(true);

        // Header
        if (subtitleLabel != null)
            subtitleLabel.setText("⚔  VS  " + opponentName);
        if (statusValue != null)
            statusValue.setText("Match Active");

        // Question
        if (question != null) {
            activeQuestion = question;
            if (problemTitle != null)
                problemTitle.setText(question.getTitle()
                        + (question.getQuestionDifficulty() != null
                                ? "  (" + question.getQuestionDifficulty() + ")"
                                : ""));
            if (problemDescription != null)
                problemDescription.setText(question.getContent());
            if (codeArea != null)
                codeArea.setText(question.getBoilerplateCode() != null
                        ? question.getBoilerplateCode()
                        : "");
        }

        if (outputArea != null)
            outputArea.setText("");

        // Enable buttons
        if (runButton != null)
            runButton.setDisable(false);
        if (submitButton != null)
            submitButton.setDisable(false);

        // Start timer
        startTimer();
    }

    // ====================================================================
    // Timer
    // ====================================================================

    private void startTimer() {
        elapsedSeconds = 0;
        if (matchTimer != null)
            matchTimer.stop();

        matchTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedSeconds++;
            int mins = elapsedSeconds / 60;
            int secs = elapsedSeconds % 60;
            if (timerLabel != null)
                timerLabel.setText(String.format("⏱ %02d:%02d", mins, secs));
        }));
        matchTimer.setCycleCount(Timeline.INDEFINITE);
        matchTimer.play();
    }

    // ====================================================================
    // Code execution
    // ====================================================================

    private void handleRunCode() {
        if (activeQuestion == null || codeArea == null)
            return;
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            if (outputArea != null)
                outputArea.setText("Please write some code first!");
            return;
        }

        String sampleInput = (activeQuestion.getTestCases() != null
                && !activeQuestion.getTestCases().isEmpty())
                        ? activeQuestion.getTestCases().get(0).getInput()
                        : "";

        if (outputArea != null)
            outputArea.setText("Running with sample input: "
                    + (sampleInput.isEmpty() ? "(none)" : sampleInput) + "\n");
        runButton.setDisable(true);

        final String finalInput = sampleInput;
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return codeExecutionService.executeJavaCode(code, finalInput);
            }
        };
        task.setOnSucceeded(e -> {
            if (outputArea != null)
                outputArea.setText("Output:\n" + task.getValue());
            runButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            if (outputArea != null)
                outputArea.setText("An error occurred during execution.");
            runButton.setDisable(false);
        });
        new Thread(task).start();
    }

    private void handleSubmitCode() {
        if (activeQuestion == null || codeArea == null)
            return;
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            if (outputArea != null)
                outputArea.setText("Please write some code first!");
            return;
        }

        if (outputArea != null)
            outputArea.setText("Evaluating all test cases...\n");
        submitButton.setDisable(true);
        runButton.setDisable(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return evaluationService.evaluate(activeQuestion, code);
            }
        };
        task.setOnSucceeded(e -> {
            boolean passed = task.getValue();
            if (passed) {
                if (outputArea != null)
                    outputArea.setText("🎉 Congratulations! You passed all test cases!");
            } else {
                if (outputArea != null)
                    outputArea.setText("❌ Some test cases failed. Please try again.");
            }
            submitButton.setDisable(false);
            runButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            if (outputArea != null)
                outputArea.setText("A system error occurred during evaluation.");
            submitButton.setDisable(false);
            runButton.setDisable(false);
        });
        new Thread(task).start();
    }

    // ====================================================================
    // Style / wiring helpers
    // ====================================================================

    private void applyLobbyStyles() {
        if (findMatchButton != null)
            findMatchButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED, Styles.LARGE);
        if (lobbyTitle != null)
            lobbyTitle.getStyleClass().add(Styles.TITLE_2);
        if (lobbySubtitle != null)
            lobbySubtitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (lobbyStatus != null)
            lobbyStatus.getStyleClass().add(Styles.TEXT_SUBTLE);

        // Match pane cards
        styleCard(statusCard);
        styleCard(challengeCard);
        styleCard(editorCard);
        if (scoreCard != null) {
            scoreCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            scoreCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
    }

    private void styleCard(VBox card) {
        if (card == null)
            return;
        card.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
        card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
    }

    private void wireButtons() {
        if (findMatchButton != null)
            findMatchButton.setOnAction(e -> onFindMatchClicked());
        if (runButton != null) {
            runButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            runButton.setOnAction(e -> handleRunCode());
        }
        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            submitButton.setOnAction(e -> handleSubmitCode());
        }
    }
}

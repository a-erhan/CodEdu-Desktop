package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.matchmaking.GameRoom;
import com.codedu.models.user.User;
import com.codedu.services.CodeExecutionService;
import com.codedu.services.MatchmakingService;
import com.codedu.services.DailyChallengeService;
import com.codedu.services.QuestionEvaluationService;
import com.codedu.services.WebSocketClientService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MatchmakingController {

    // -------------------------------------------------------------------------
    // FXML bindings
    // -------------------------------------------------------------------------
    @FXML private Label titleLabel;
    @FXML private Label timeLabel;
    @FXML private Label subtitleLabel;
    @FXML private VBox statusCard;
    @FXML private Label statusLabel;
    @FXML private Label statusValue;
    @FXML private VBox challengeCard;
    @FXML private Label challengeHeader;
    @FXML private Label problemTitle;
    @FXML private Label problemDescription;
    @FXML private VBox editorCard;
    @FXML private Label editorLabel;
    @FXML private TextArea codeArea;
    @FXML private HBox actionRow;
    @FXML private Button runButton;
    @FXML private Button submitButton;
    @FXML private Label outputLabel;
    @FXML private TextArea outputArea;
    @FXML private VBox scoreCard;
    @FXML private Label scoreTitle;
    @FXML private Label youScore;
    @FXML private Label opponentScore;
    @FXML private Label attemptsTitle;
    @FXML private Label youAttempts;
    @FXML private Label opponentAttempts;
    @FXML private HBox navigationRow;
    @FXML private Button easyNavBtn;
    @FXML private Button mediumNavBtn;
    @FXML private Button hardNavBtn;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private User currentUser;
    private CodeImplementationQuestion activeQuestion;
    private List<CodeImplementationQuestion> matchQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private final List<String> userSolutions = new ArrayList<>(java.util.Arrays.asList("", "", ""));
    private final List<Boolean> questionsPassed = new ArrayList<>(java.util.Arrays.asList(false, false, false));

    // -------------------------------------------------------------------------
    // Backend services
    // -------------------------------------------------------------------------
    private final CodeExecutionService codeExecutionService;
    private final MatchmakingService matchmakingService;
    private final DailyChallengeService dailyChallengeService;
    private final QuestionEvaluationService evaluationService;
    private final com.codedu.repositories.interfaces.QuestionRepository questionRepository;
    private final WebSocketClientService webSocketClientService;

    @Autowired
    public MatchmakingController(CodeExecutionService codeExecutionService,
                                 MatchmakingService matchmakingService,
                                 DailyChallengeService dailyChallengeService,
                                 QuestionEvaluationService evaluationService,
                                 com.codedu.repositories.interfaces.QuestionRepository questionRepository,
                                 WebSocketClientService webSocketClientService) {
        this.codeExecutionService = codeExecutionService;
        this.matchmakingService = matchmakingService;
        this.dailyChallengeService = dailyChallengeService;
        this.evaluationService = evaluationService;
        this.questionRepository = questionRepository;
        this.webSocketClientService = webSocketClientService;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        applyStyles();
        wireButtons();
        showSearchingState();
        // Actual queue join happens in setCurrentUser(), called by MainShellController
        // after the FXML has been loaded and this controller is fully constructed.
    }

    /**
     * Called by {@code MainShellController} immediately after FXML loading.
     * Stores the logged-in user, then joins the real matchmaking queue.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        joinMatchmakingQueue();
    }

    // -------------------------------------------------------------------------
    // Matchmaking queue
    // -------------------------------------------------------------------------

    private void joinMatchmakingQueue() {
        if (currentUser == null) return;

        // Single call: opens the STOMP session, subscribes to /queue/match/{id},
        // then sends /app/match.join — all inside afterConnected on the same TCP
        // connection.  This eliminates the subscribe/join race condition that
        // previously caused match notifications to be silently dropped.
        webSocketClientService.connectAndJoinMatchmaking(currentUser.getId(), this::onMatchFound);
    }

    /**
     * Called on the STOMP receive thread when the server broadcasts a {@link GameRoom}.
     * All JavaFX UI mutations are wrapped in {@code Platform.runLater()}.
     */
    private void onMatchFound(GameRoom gameRoom) {
        // Determine opponent: player1 is whoever joined first.
        User opponent = (gameRoom.getPlayer1().getId() == currentUser.getId())
                ? gameRoom.getPlayer2()
                : gameRoom.getPlayer1();

        // Reload full question from DB so testCases are available for code evaluation.
        // (testCases are @JsonIgnored in the wire DTO to avoid lazy-loading errors.)
        CodeImplementationQuestion fullQuestion = null;
        if (gameRoom.getQuestion() != null) {
            fullQuestion = (CodeImplementationQuestion) questionRepository
                    .findById(gameRoom.getQuestion().getId())
                    .orElse(gameRoom.getQuestion());
        }

        final CodeImplementationQuestion question = fullQuestion;
        final String opponentName = opponent != null ? opponent.getUsername() : "Opponent";

        Platform.runLater(() -> showMatchFoundState(opponentName, question));
    }

    // -------------------------------------------------------------------------
    // UI state transitions
    // -------------------------------------------------------------------------

    /** Initial "waiting" state shown as soon as the screen opens. */
    private void showSearchingState() {
        if (statusValue != null)
            statusValue.setText("Searching for opponent...");
        if (subtitleLabel != null)
            subtitleLabel.setText("You'll be matched with a player of similar level.");

        disableActionButtons(true);
        disableNavButtons(true);

        if (problemTitle != null)
            problemTitle.setText("Waiting for match...");
        if (problemDescription != null)
            problemDescription.setText("A coding challenge will appear here once you're matched.");
        if (codeArea != null)
            codeArea.setText("");
    }

    /** Called on the JavaFX thread once the server confirms a match. */
    private void showMatchFoundState(String opponentName, CodeImplementationQuestion question) {
        if (statusValue != null)
            statusValue.setText("Match Active");
        if (subtitleLabel != null)
            subtitleLabel.setText("VS  " + opponentName);

        if (question != null) {
            activeQuestion = question;
            matchQuestions.clear();
            matchQuestions.add(question);
            currentQuestionIndex = 0;

            if (problemTitle != null)
                problemTitle.setText(question.getTitle()
                        + (question.getQuestionDifficulty() != null
                        ? "  (" + question.getQuestionDifficulty() + ")" : ""));
            if (problemDescription != null)
                problemDescription.setText(question.getContent());
            if (codeArea != null)
                codeArea.setText(question.getBoilerplateCode() != null
                        ? question.getBoilerplateCode() : "");
        }

        if (outputArea != null)
            outputArea.setText("");

        disableActionButtons(false);   // unlock Run / Submit
        disableNavButtons(false);
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    private void switchQuestion(int index) {
        if (index < 0 || index >= matchQuestions.size()) return;

        if (activeQuestion != null && codeArea != null)
            userSolutions.set(currentQuestionIndex, codeArea.getText());

        currentQuestionIndex = index;
        loadCurrentQuestionToUI();
        updateNavigationStyles();
    }

    private void updateNavigationStyles() {
        if (easyNavBtn == null || mediumNavBtn == null || hardNavBtn == null) return;

        Button[] btns = {easyNavBtn, mediumNavBtn, hardNavBtn};
        for (int i = 0; i < btns.length; i++) {
            btns[i].getStyleClass().removeAll(Styles.SUCCESS, Styles.ACCENT, Styles.BUTTON_OUTLINED);
            if (i == currentQuestionIndex) {
                btns[i].getStyleClass().add(Styles.ACCENT);
            } else if (i < questionsPassed.size() && questionsPassed.get(i)) {
                btns[i].getStyleClass().add(Styles.SUCCESS);
            } else {
                btns[i].getStyleClass().add(Styles.BUTTON_OUTLINED);
            }
        }
    }

    private void loadCurrentQuestionToUI() {
        if (currentQuestionIndex >= matchQuestions.size()) return;
        activeQuestion = matchQuestions.get(currentQuestionIndex);
        if (activeQuestion == null) return;

        if (problemTitle != null)
            problemTitle.setText(activeQuestion.getTitle()
                    + (activeQuestion.getQuestionDifficulty() != null
                    ? "  (" + activeQuestion.getQuestionDifficulty() + ")" : ""));
        if (problemDescription != null)
            problemDescription.setText(activeQuestion.getContent());

        if (codeArea != null) {
            String saved = currentQuestionIndex < userSolutions.size()
                    ? userSolutions.get(currentQuestionIndex) : "";
            codeArea.setText((saved == null || saved.trim().isEmpty())
                    ? (activeQuestion.getBoilerplateCode() != null ? activeQuestion.getBoilerplateCode() : "")
                    : saved);
        }
        if (outputArea != null) outputArea.setText("");
        updateNavigationStyles();
    }

    // -------------------------------------------------------------------------
    // Code execution
    // -------------------------------------------------------------------------

    private void handleRunCode() {
        if (activeQuestion == null || codeArea == null) return;
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            if (outputArea != null) outputArea.setText("Please write some code first!");
            return;
        }

        String sampleInput = (activeQuestion.getTestCases() != null && !activeQuestion.getTestCases().isEmpty())
                ? activeQuestion.getTestCases().get(0).getInput() : "";

        if (outputArea != null)
            outputArea.setText("Executing Sample Input: " + (sampleInput.isEmpty() ? "(none)" : sampleInput) + "\n");
        runButton.setDisable(true);

        final String finalInput = sampleInput;
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return codeExecutionService.executeJavaCode(code, finalInput);
            }
        };
        task.setOnSucceeded(e -> {
            if (outputArea != null) outputArea.setText("Output:\n" + task.getValue());
            runButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            if (outputArea != null) outputArea.setText("An error occurred during execution.");
            runButton.setDisable(false);
        });
        new Thread(task).start();
    }

    private void handleSubmitCode() {
        if (activeQuestion == null || codeArea == null) return;
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            if (outputArea != null) outputArea.setText("Please write some code first!");
            return;
        }

        if (outputArea != null) outputArea.setText("Evaluating all test cases...\n");
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
                    outputArea.setText("Congratulations! You passed all test cases.");
                if (currentQuestionIndex < questionsPassed.size())
                    questionsPassed.set(currentQuestionIndex, true);
                updateNavigationStyles();
            } else {
                if (outputArea != null)
                    outputArea.setText("Some test cases failed. Please try again.");
            }
            submitButton.setDisable(false);
            runButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            if (outputArea != null) outputArea.setText("A system error occurred during evaluation.");
            submitButton.setDisable(false);
            runButton.setDisable(false);
        });
        new Thread(task).start();
    }

    // -------------------------------------------------------------------------
    // Style / wiring helpers
    // -------------------------------------------------------------------------

    private void applyStyles() {
        if (titleLabel != null) titleLabel.getStyleClass().add(Styles.TITLE_3);
        styleCard(statusCard);
        if (statusLabel != null) statusLabel.getStyleClass().add(Styles.TEXT_BOLD);
        if (statusValue != null) statusValue.getStyleClass().add(Styles.TEXT_SUBTLE);
        styleCard(challengeCard);
        if (challengeHeader != null) challengeHeader.getStyleClass().add(Styles.TEXT_BOLD);
        if (problemTitle != null) problemTitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        styleCard(editorCard);
        if (editorLabel != null) editorLabel.getStyleClass().add(Styles.TEXT_BOLD);
        if (outputLabel != null) outputLabel.getStyleClass().add(Styles.TEXT_BOLD);
        if (scoreCard != null) {
            scoreCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            scoreCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (scoreTitle != null) scoreTitle.getStyleClass().add(Styles.TEXT_BOLD);
        if (youScore != null) youScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (opponentScore != null) opponentScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (attemptsTitle != null) attemptsTitle.getStyleClass().add(Styles.TEXT_BOLD);
        if (youAttempts != null) youAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (opponentAttempts != null) opponentAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);
    }

    private void styleCard(VBox card) {
        if (card == null) return;
        card.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
        card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
    }

    private void wireButtons() {
        if (runButton != null) {
            runButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            runButton.setOnAction(e -> handleRunCode());
        }
        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            submitButton.setOnAction(e -> handleSubmitCode());
        }
        if (easyNavBtn != null) easyNavBtn.setOnAction(e -> switchQuestion(0));
        if (mediumNavBtn != null) mediumNavBtn.setOnAction(e -> switchQuestion(1));
        if (hardNavBtn != null) hardNavBtn.setOnAction(e -> switchQuestion(2));
    }

    private void disableActionButtons(boolean disable) {
        if (runButton != null) runButton.setDisable(disable);
        if (submitButton != null) submitButton.setDisable(disable);
    }

    private void disableNavButtons(boolean disable) {
        if (easyNavBtn != null) easyNavBtn.setDisable(disable);
        if (mediumNavBtn != null) mediumNavBtn.setDisable(disable);
        if (hardNavBtn != null) hardNavBtn.setDisable(disable);
    }
}

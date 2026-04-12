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

@Controller
public class MatchmakingController {

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
    private Button submitButton;
    @FXML
    private Button resignButton;
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

    @FXML
    private VBox resultPane;
    @FXML
    private Label resultTitle;
    @FXML
    private Label resultSubtitle;
    @FXML
    private Label resultTime;
    @FXML
    private Label resultYouAvatar;
    @FXML
    private Label resultYouAttempts;
    @FXML
    private Label resultOpponentAvatar;
    @FXML
    private Label resultOpponentName;
    @FXML
    private Label resultOpponentAttempts;
    @FXML
    private Label resultRewardLabel;
    @FXML
    private Button backToLobbyButton;

    private User currentUser;
    private GameRoom activeRoom;
    private User activeOpponent;
    private CodeImplementationQuestion activeQuestion;
    private int elapsedSeconds = 0;
    private Timeline matchTimer;
    private int localAttempts = 0;
    private int opponentAttemptsCount = 0;
    private java.util.function.Consumer<String> onOpenProfile;

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

    @FXML
    public void initialize() {
        showLobby();
        applyLobbyStyles();
        wireButtons();

        Platform.runLater(() -> {
            if (lobbyPane != null && lobbyPane.getParent() != null) {
                lobbyPane.getParent().sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene == null) {
                        System.out.println("[MC] View removed from scene. Cleaning up matchmaking state.");
                        cleanupMatchmaking();
                    }
                });
            }
        });
    }

    private boolean matchEnded = false;

    private void cleanupMatchmaking() {
        if (currentUser == null) return;
        if (activeRoom != null && !matchEnded) {
            System.out.println("[MC] Navigated away during active match. Auto-resigning.");
            handleResign();
        }
        webSocketClientService.leaveMatchmaking(currentUser.getId());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setOnOpenProfile(java.util.function.Consumer<String> onOpenProfile) {
        this.onOpenProfile = onOpenProfile;
    }

    private void onFindMatchClicked() {
        if (currentUser == null)
            return;

        findMatchButton.setDisable(true);
        lobbyStatus.setText("Searching for opponent...");
        System.out.println("[MC] Find Match clicked. Connecting to matchmaking...");

        webSocketClientService.connectAndJoinMatchmaking(
                currentUser.getId(), this::onMatchFound, this::onMatchResult, this::onAttemptReceived);
    }

    private void onMatchFound(GameRoom gameRoom) {
        System.out.println("[MC] >>> onMatchFound! Room: " + gameRoom.getRoomId());

        this.activeRoom = gameRoom;

        User opponent = (gameRoom.getPlayer1().getId() == currentUser.getId())
                ? gameRoom.getPlayer2()
                : gameRoom.getPlayer1();

        this.activeOpponent = opponent;

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
        final String opponentName = opponent != null ? opponent.getDisplayName() : "Opponent";

        Platform.runLater(() -> showMatch(opponentName, question));
    }

    private void onMatchResult(com.codedu.models.matchmaking.MatchResult result) {
        System.out.println("[MC] >>> onMatchResult! Winner: " + result.getWinnerId());
        Platform.runLater(() -> {
            matchEnded = true;
            boolean won = (currentUser != null && result.getWinnerId() == currentUser.getId());
            showResultPane(won);
        });
    }

    private void onAttemptReceived(com.codedu.models.matchmaking.MatchAttemptUpdate update) {
        Platform.runLater(() -> {
            opponentAttemptsCount = update.getAttempts();
            if (opponentAttempts != null) {
                opponentAttempts.setText("Opponent: " + opponentAttemptsCount);
            }
        });
    }

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

        if (lobbyPane != null)
            lobbyPane.setVisible(false);
        if (matchPane != null)
            matchPane.setVisible(true);

        if (subtitleLabel != null)
            subtitleLabel.setText("⚔  VS  " + opponentName);
        if (statusValue != null)
            statusValue.setText("Match Active");

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

        matchEnded = false;
        if (submitButton != null)
            submitButton.setDisable(false);
        if (resignButton != null)
            resignButton.setDisable(false);
        if (codeArea != null)
            codeArea.setDisable(false);

        localAttempts = 0;
        opponentAttemptsCount = 0;
        if (youAttempts != null) youAttempts.setText("You: 0");
        if (opponentAttempts != null) opponentAttempts.setText("Opponent: 0");

        startTimer();
    }

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

        localAttempts++;
        if (youAttempts != null) {
            youAttempts.setText("You: " + localAttempts);
        }
        if (activeRoom != null && activeOpponent != null) {
            com.codedu.models.matchmaking.MatchAttemptUpdate update = new com.codedu.models.matchmaking.MatchAttemptUpdate(
                activeRoom.getRoomId(), currentUser.getId(), activeOpponent.getId(), localAttempts
            );
            webSocketClientService.sendAttemptUpdate(update);
        }

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

                if (activeRoom != null && activeOpponent != null) {
                    com.codedu.models.matchmaking.MatchResult result = new com.codedu.models.matchmaking.MatchResult(
                        activeRoom.getRoomId(),
                        currentUser.getId(),
                        activeOpponent.getId(),
                        currentUser.getUsername()
                    );
                    webSocketClientService.sendMatchResult(result);
                }
            } else {
                if (outputArea != null)
                    outputArea.setText("❌ Some test cases failed. Please try again.");
            }
            submitButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            if (outputArea != null)
                outputArea.setText("A system error occurred during evaluation.");
            submitButton.setDisable(false);
        });
        new Thread(task).start();
    }

    private void handleResign() {
        if (activeRoom == null || activeOpponent == null || matchEnded) return;

        System.out.println("[MC] Resign clicked/triggered.");
        matchEnded = true;
        if (resignButton != null) resignButton.setDisable(true);
        if (submitButton != null) submitButton.setDisable(true);
        if (codeArea != null) codeArea.setDisable(true);

        com.codedu.models.matchmaking.MatchResult result = new com.codedu.models.matchmaking.MatchResult(
            activeRoom.getRoomId(),
            activeOpponent.getId(),
            currentUser.getId(),
            activeOpponent.getUsername()
        );
        webSocketClientService.sendMatchResult(result);
    }

    private void showResultPane(boolean won) {
        if (matchTimer != null) matchTimer.stop();
        if (matchPane != null) matchPane.setVisible(false);
        if (resultPane != null) resultPane.setVisible(true);

        if (resultTitle != null) {
            resultTitle.setText(won ? "Match Won!" : "Match Lost!");
            resultTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + (won ? "-color-success-emphasis;" : "-color-danger-emphasis;"));
        }

        int mins = elapsedSeconds / 60;
        int secs = elapsedSeconds % 60;
        if (resultTime != null) {
            resultTime.setText(String.format("Time: %02d:%02d", mins, secs));
        }

        if (resultYouAttempts != null) resultYouAttempts.setText(localAttempts + " Attempts");
        if (resultOpponentAttempts != null) resultOpponentAttempts.setText(opponentAttemptsCount + " Attempts");

        if (resultYouAvatar != null && currentUser != null) {
            String you = currentUser.getDisplayName();
            resultYouAvatar.setText(you.isEmpty() ? "?" : you.substring(0, 1).toUpperCase());
            resultYouAvatar.setOnMouseClicked(e -> { if (onOpenProfile != null) onOpenProfile.accept(currentUser.getUsername()); });
        }

        if (resultOpponentAvatar != null && activeOpponent != null) {
            String opp = activeOpponent.getDisplayName();
            resultOpponentAvatar.setText(opp.isEmpty() ? "?" : opp.substring(0, 1).toUpperCase());
            resultOpponentName.setText(opp);
            resultOpponentAvatar.setOnMouseClicked(e -> { if (onOpenProfile != null) onOpenProfile.accept(activeOpponent.getUsername()); });
        }

        if (resultRewardLabel != null) {
            resultRewardLabel.setText(won ? "+50 XP ✨  +50 Tokens 🪙" : "-50 XP 📉  -50 Tokens 💸");
            resultRewardLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + (won ? "-color-success-emphasis;" : "-color-danger-emphasis;"));
        }

        if (won) {
            com.codedu.ui.UIUtils.fireConfetti((javafx.scene.layout.Pane) resultPane.getParent());
        }
    }

    private void applyLobbyStyles() {
        if (findMatchButton != null)
            findMatchButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED, Styles.LARGE);
        if (lobbyTitle != null)
            lobbyTitle.getStyleClass().add(Styles.TITLE_2);
        if (lobbySubtitle != null)
            lobbySubtitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (lobbyStatus != null)
            lobbyStatus.getStyleClass().add(Styles.TEXT_SUBTLE);

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
        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            submitButton.setOnAction(e -> handleSubmitCode());
        }
        if (resignButton != null) {
            resignButton.getStyleClass().addAll(Styles.DANGER, Styles.ROUNDED);
            resignButton.setOnAction(e -> handleResign());
        }
        if (backToLobbyButton != null) {
            backToLobbyButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            backToLobbyButton.setOnAction(e -> {
                if (resultPane != null) resultPane.setVisible(false);
                showLobby();
            });
        }
    }
}

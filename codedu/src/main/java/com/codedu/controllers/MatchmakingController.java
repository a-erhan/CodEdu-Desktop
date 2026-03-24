package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.TestCase;
import com.codedu.services.CodeExecutionService;
import com.codedu.services.MatchmakingService;
import com.codedu.services.DailyChallengeService;
import com.codedu.services.QuestionEvaluationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;

@Controller
public class MatchmakingController {

    // --- UI Elements ---
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

    private CodeImplementationQuestion activeQuestion;

    // --- Backend Services ---
    private final CodeExecutionService codeExecutionService;
    private final MatchmakingService matchmakingService;
    private final DailyChallengeService dailyChallengeService;
    private final QuestionEvaluationService evaluationService;

    @Autowired
    public MatchmakingController(CodeExecutionService codeExecutionService,
                                 MatchmakingService matchmakingService,
                                 DailyChallengeService dailyChallengeService,
                                 QuestionEvaluationService evaluationService) {
        this.codeExecutionService = codeExecutionService;
        this.matchmakingService = matchmakingService;
        this.dailyChallengeService = dailyChallengeService;
        this.evaluationService = evaluationService;
    }

    // --- Initialization ---
    @FXML
    public void initialize() {
        if (titleLabel != null) titleLabel.getStyleClass().add(Styles.TITLE_3);
        if (statusCard != null) {
            statusCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            statusCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (statusLabel != null) statusLabel.getStyleClass().add(Styles.TEXT_BOLD);
        if (statusValue != null) statusValue.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (challengeCard != null) {
            challengeCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            challengeCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (challengeHeader != null) challengeHeader.getStyleClass().add(Styles.TEXT_BOLD);
        if (problemTitle != null) problemTitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        if (editorCard != null) {
            editorCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            editorCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
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

        if (runButton != null) {
            runButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            runButton.setOnAction(e -> handleRunCode());
        }

        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            submitButton.setOnAction(e -> handleSubmitCode());
        }

        createMockCodingQuestion();
    }

    // --- Mock Data Setup ---
    private void createMockCodingQuestion() {
        if (statusValue != null) statusValue.setText("Practice Mode Active");

        TestCase tc1 = new TestCase();
        tc1.setInput("5 3");
        tc1.setExpectedOutput("8");

        TestCase tc2 = new TestCase();
        tc2.setInput("-2 7");
        tc2.setExpectedOutput("5");

        activeQuestion = new CodeImplementationQuestion();
        activeQuestion.setTitle("Two Sum (Console)");
        activeQuestion.setContent("Write a Java program that reads two integers using Scanner and prints their sum.\n\nSample Input: 5 3\nSample Output: 8");
        activeQuestion.setTestCases(Arrays.asList(tc1, tc2));
        activeQuestion.setBoilerplateCode("import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        \n        \n    }\n}");

        if (problemTitle != null) problemTitle.setText(activeQuestion.getTitle());
        if (problemDescription != null) problemDescription.setText(activeQuestion.getContent());

        if (codeArea != null) {
            codeArea.setText(activeQuestion.getBoilerplateCode());
        }
        if (runButton != null) runButton.setDisable(false);
        if (submitButton != null) submitButton.setDisable(false);
    }

    // --- Execution Handling ---
    private void handleRunCode() {
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            outputArea.setText("Please write some code first!");
            return;
        }

        outputArea.setText("Executing via JDoodle API...\n");
        runButton.setDisable(true);

        Task<String> executionTask = new Task<>() {
            @Override
            protected String call() {
                // "Run" butonu sadece tek bir örnek girdiyi (10 20) dener.
                return codeExecutionService.executeJavaCode(code, "10 20");
            }
        };

        executionTask.setOnSucceeded(event -> {
            outputArea.setText("Result for Sample Input (10 20):\n" + executionTask.getValue());
            runButton.setDisable(false);
        });

        executionTask.setOnFailed(event -> {
            outputArea.setText("An error occurred during execution.");
            runButton.setDisable(false);
        });

        new Thread(executionTask).start();
    }

    private void handleSubmitCode() {
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            outputArea.setText("Please write some code first!");
            return;
        }

        outputArea.setText("Evaluating all test cases...\n");
        submitButton.setDisable(true);
        runButton.setDisable(true);

        Task<Boolean> evaluateTask = new Task<>() {
            @Override
            protected Boolean call() {
                // "Submit" butonu arka planda o zeki QuestionEvaluationService'i çağırır.
                // O servis de CodeImplementationQuestion'ın içindeki tc1 (5 3) ve tc2 (-2 7)
                // olmak üzere TÜM testcase'leri sırayla JDoodle'a gönderip kontrol eder.
                return evaluationService.evaluate(activeQuestion, code);
            }
        };

        evaluateTask.setOnSucceeded(event -> {
            boolean passedAll = evaluateTask.getValue();
            if (passedAll) {
                outputArea.setText("Congratulations! You passed all test cases.\nDealing damage to the opponent...");
            } else {
                outputArea.setText("Some test cases failed or your code threw an error. Please try again.");
            }
            submitButton.setDisable(false);
            runButton.setDisable(false);
        });

        evaluateTask.setOnFailed(event -> {
            outputArea.setText("A system error occurred during evaluation.");
            submitButton.setDisable(false);
            runButton.setDisable(false);
        });

        new Thread(evaluateTask).start();
    }
}
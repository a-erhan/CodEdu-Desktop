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
import java.util.List;
import java.util.ArrayList;

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

    @FXML
    private HBox navigationRow;
    @FXML
    private Button easyNavBtn;
    @FXML
    private Button mediumNavBtn;
    @FXML
    private Button hardNavBtn;

    private CodeImplementationQuestion activeQuestion;
    private List<CodeImplementationQuestion> matchQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private final List<String> userSolutions = new ArrayList<>(java.util.Arrays.asList("", "", ""));
    private final List<Boolean> questionsPassed = new ArrayList<>(java.util.Arrays.asList(false, false, false));

    // --- Backend Services ---
    private final CodeExecutionService codeExecutionService;
    private final MatchmakingService matchmakingService;
    private final DailyChallengeService dailyChallengeService;
    private final QuestionEvaluationService evaluationService;
    private final com.codedu.repositories.interfaces.QuestionRepository questionRepository;

    @Autowired
    public MatchmakingController(CodeExecutionService codeExecutionService,
            MatchmakingService matchmakingService,
            DailyChallengeService dailyChallengeService,
            QuestionEvaluationService evaluationService,
            com.codedu.repositories.interfaces.QuestionRepository questionRepository) {
        this.codeExecutionService = codeExecutionService;
        this.matchmakingService = matchmakingService;
        this.dailyChallengeService = dailyChallengeService;
        this.evaluationService = evaluationService;
        this.questionRepository = questionRepository;
    }

    // --- Initialization ---
    @FXML
    public void initialize() {
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

        if (runButton != null) {
            runButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            runButton.setOnAction(e -> handleRunCode());
        }

        if (easyNavBtn != null)
            easyNavBtn.setOnAction(e -> switchQuestion(0));
        if (mediumNavBtn != null)
            mediumNavBtn.setOnAction(e -> switchQuestion(1));
        if (hardNavBtn != null)
            hardNavBtn.setOnAction(e -> switchQuestion(2));
        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            submitButton.setOnAction(e -> handleSubmitCode());
        }

        loadMatchQuestions();
    }

    private void switchQuestion(int index) {
        if (index < 0 || index >= matchQuestions.size())
            return;

        // Save current solution
        if (activeQuestion != null && codeArea != null) {
            userSolutions.set(currentQuestionIndex, codeArea.getText());
        }

        currentQuestionIndex = index;
        loadCurrentQuestionToUI();
        updateNavigationStyles();
    }

    private void updateNavigationStyles() {
        if (easyNavBtn == null || mediumNavBtn == null || hardNavBtn == null)
            return;

        Button[] btns = { easyNavBtn, mediumNavBtn, hardNavBtn };
        for (int i = 0; i < btns.length; i++) {
            btns[i].getStyleClass().removeAll(Styles.SUCCESS, Styles.ACCENT, Styles.BUTTON_OUTLINED);
            if (i == currentQuestionIndex) {
                btns[i].getStyleClass().add(Styles.ACCENT);
            } else if (questionsPassed.get(i)) {
                btns[i].getStyleClass().add(Styles.SUCCESS);
            } else {
                btns[i].getStyleClass().add(Styles.BUTTON_OUTLINED);
            }
        }
    }

    // --- Data Setup ---
    private void loadMatchQuestions() {
        if (statusValue != null)
            statusValue.setText("Match Active");

        matchQuestions.clear();
        currentQuestionIndex = 0;

        matchQuestions.add(getRandomQuestionByDiff(com.codedu.models.learning.QuestionDifficulity.EASY));
        matchQuestions.add(getRandomQuestionByDiff(com.codedu.models.learning.QuestionDifficulity.MEDIUM));
        matchQuestions.add(getRandomQuestionByDiff(com.codedu.models.learning.QuestionDifficulity.HARD));

        loadCurrentQuestionToUI();
    }

    private CodeImplementationQuestion getRandomQuestionByDiff(
            com.codedu.models.learning.QuestionDifficulity difficulity) {
        java.util.List<com.codedu.models.learning.Question> questions = questionRepository
                .findByQuestionDifficulity(difficulity);
        java.util.List<CodeImplementationQuestion> codeQs = questions.stream()
                .filter(q -> q instanceof CodeImplementationQuestion)
                .map(q -> (CodeImplementationQuestion) q)
                .collect(java.util.stream.Collectors.toList());

        if (codeQs.isEmpty()) {
            return null;
        }
        java.util.Random rand = new java.util.Random();
        return codeQs.get(rand.nextInt(codeQs.size()));
    }

    private void loadCurrentQuestionToUI() {
        if (currentQuestionIndex >= matchQuestions.size()) {
            if (problemTitle != null)
                problemTitle.setText("Match Completed!");
            if (problemDescription != null)
                problemDescription.setText("You have passed all the challenges!");
            if (codeArea != null)
                codeArea.setText("");
            if (runButton != null)
                runButton.setDisable(true);
            if (submitButton != null)
                submitButton.setDisable(true);
            return;
        }

        activeQuestion = matchQuestions.get(currentQuestionIndex);
        if (activeQuestion == null) {
            System.err.println("Question not found for index " + currentQuestionIndex);
            return;
        }

        if (problemTitle != null)
            problemTitle.setText(activeQuestion.getTitle() + " (" + activeQuestion.getQuestionDifficulity() + ")");
        if (problemDescription != null)
            problemDescription.setText(activeQuestion.getContent());

        if (codeArea != null) {
            String existingSol = userSolutions.get(currentQuestionIndex);
            if (existingSol == null || existingSol.trim().isEmpty()) {
                codeArea.setText(activeQuestion.getBoilerplateCode());
            } else {
                codeArea.setText(existingSol);
            }
        }
        if (runButton != null)
            runButton.setDisable(false);
        if (submitButton != null)
            submitButton.setDisable(false);
        if (outputArea != null)
            outputArea.setText("");

        updateNavigationStyles();
    }

    // --- Execution Handling ---
    private void handleRunCode() {
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            outputArea.setText("Please write some code first!");
            return;
        }

        // Use the first test case's input for the "Run" feature
        String sampleInput = "";
        if (activeQuestion != null && !activeQuestion.getTestCases().isEmpty()) {
            sampleInput = activeQuestion.getTestCases().get(0).getInput();
        }

        outputArea.setText("Executing Sample Input: " + (sampleInput.isEmpty() ? "(none)" : sampleInput) + "\n");
        runButton.setDisable(true);

        final String finalInput = sampleInput;
        Task<String> executionTask = new Task<>() {
            @Override
            protected String call() {
                return codeExecutionService.executeJavaCode(code, finalInput);
            }
        };

        executionTask.setOnSucceeded(event -> {
            outputArea.setText("Output:\n" + executionTask.getValue());
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
                return evaluationService.evaluate(activeQuestion, code);
            }
        };

        evaluateTask.setOnSucceeded(event -> {
            boolean passedAll = evaluateTask.getValue();
            if (passedAll) {
                outputArea.setText("Congratulations! You passed all test cases for this challenge.");
                questionsPassed.set(currentQuestionIndex, true);
                updateNavigationStyles();

                if (currentQuestionIndex < matchQuestions.size() - 1) {
                    outputArea.appendText("\nYou can move to the next challenge using the buttons above.");
                } else {
                    boolean allDone = questionsPassed.stream().allMatch(p -> p);
                    if (allDone) {
                        outputArea.appendText("\nMatch finished! You've completed all difficulty levels.");
                    }
                }
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
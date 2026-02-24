package com.codedu.controllers;

import com.codedu.models.Chapter;
import com.codedu.models.ChapterContent;
import com.codedu.models.Question;
import com.codedu.models.QuestionType;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the interactive chapter content view.
 * Renders Learn / Quiz / Practice sections for a given chapter.
 */
@Controller
public class ChapterViewController {

    @FXML
    private Button btnBack;
    @FXML
    private Label headerTitle;
    @FXML
    private Label headerXP;
    @FXML
    private HBox tabBar;
    @FXML
    private Button tabLearn;
    @FXML
    private Button tabQuiz;
    @FXML
    private Button tabPractice;

    @FXML
    private StackPane contentStack;
    @FXML
    private ScrollPane learnScroll;
    @FXML
    private ScrollPane quizScroll;
    @FXML
    private ScrollPane practiceScroll;
    @FXML
    private VBox learnContainer;
    @FXML
    private VBox quizContainer;
    @FXML
    private VBox practiceContainer;

    private Chapter chapter;
    private Runnable onBack;

    @FXML
    public void initialize() {
        tabLearn.setOnAction(e -> switchTab("learn"));
        tabQuiz.setOnAction(e -> switchTab("quiz"));
        tabPractice.setOnAction(e -> switchTab("practice"));
    }

    /** Called externally after FXML load. */
    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
        headerTitle.setText(chapter.getTitle());
        // Add small icon image to the left of the header title
        if (chapter.getIconImage() != null) {
            Image img = new Image(getClass().getResourceAsStream(chapter.getIconImage()));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(28);
            iv.setFitHeight(28);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            headerTitle.setGraphic(iv);
            headerTitle.setGraphicTextGap(10);
        }
        headerXP.setText("XP: " + chapter.getXpReward());

        ChapterContent content = chapter.getContent();
        if (content != null) {
            buildLearnSection(content.getLearnText());

            List<Question> questions = content.getQuestions();
            if (questions == null)
                questions = new ArrayList<>();

            List<Question> mcQuestions = questions.stream()
                    .filter(q -> q.getQuestionType() == QuestionType.MULTIPLE_CHOICES)
                    .collect(Collectors.toList());
            List<Question> fillBlanks = questions.stream()
                    .filter(q -> q.getQuestionType() == QuestionType.FILL_IN_THE_BLANKS)
                    .collect(Collectors.toList());
            List<Question> codeQuestions = questions.stream()
                    .filter(q -> q.getQuestionType() == QuestionType.CODE_IMPLEMENTATION)
                    .collect(Collectors.toList());

            buildQuizSection(mcQuestions, fillBlanks);
            buildPracticeSection(codeQuestions);
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
        btnBack.setOnAction(e -> {
            if (onBack != null)
                onBack.run();
        });
    }

    // ── Tab switching ─────────────────────────────────────────────────

    private void switchTab(String tab) {
        learnScroll.setVisible("learn".equals(tab));
        quizScroll.setVisible("quiz".equals(tab));
        practiceScroll.setVisible("practice".equals(tab));

        tabLearn.getStyleClass().remove("cv-tab-active");
        tabQuiz.getStyleClass().remove("cv-tab-active");
        tabPractice.getStyleClass().remove("cv-tab-active");

        switch (tab) {
            case "learn" -> tabLearn.getStyleClass().add("cv-tab-active");
            case "quiz" -> tabQuiz.getStyleClass().add("cv-tab-active");
            case "practice" -> tabPractice.getStyleClass().add("cv-tab-active");
        }
    }

    // ── Learn section ─────────────────────────────────────────────────

    private void buildLearnSection(String text) {
        learnContainer.getChildren().clear();

        String[] paragraphs = text.split("\n\n");
        for (String para : paragraphs) {
            para = para.trim();
            if (para.isEmpty())
                continue;

            if (para.startsWith("# ")) {
                Label h = new Label(para.substring(2));
                h.getStyleClass().add("cv-learn-h1");
                h.setWrapText(true);
                learnContainer.getChildren().add(h);
            } else if (para.startsWith("## ")) {
                Label h = new Label(para.substring(3));
                h.getStyleClass().add("cv-learn-h2");
                h.setWrapText(true);
                learnContainer.getChildren().add(h);
            } else if (para.startsWith("```")) {
                // code block
                String code = para.replaceAll("^```[a-z]*\\n?", "").replaceAll("\\n?```$", "");
                VBox codeBox = new VBox();
                codeBox.getStyleClass().add("cv-code-block");
                codeBox.setPadding(new Insets(14, 18, 14, 18));
                Label codeLabel = new Label(code);
                codeLabel.getStyleClass().add("cv-code-text");
                codeLabel.setWrapText(true);
                codeBox.getChildren().add(codeLabel);
                learnContainer.getChildren().add(codeBox);
            } else if (para.startsWith("• ") || para.startsWith("- ")) {
                // bullet list
                String[] lines = para.split("\n");
                for (String line : lines) {
                    String bullet = line.replaceFirst("^[•\\-] *", "");
                    Label item = new Label("  •  " + bullet);
                    item.getStyleClass().add("cv-learn-bullet");
                    item.setWrapText(true);
                    learnContainer.getChildren().add(item);
                }
            } else {
                Label p = new Label(para);
                p.getStyleClass().add("cv-learn-text");
                p.setWrapText(true);
                learnContainer.getChildren().add(p);
            }
        }
    }

    // ── Quiz section ──────────────────────────────────────────────────

    private void buildQuizSection(List<Question> mcqs, List<Question> fills) {
        quizContainer.getChildren().clear();

        // Section header: MCQ
        Label mcqTitle = new Label("Multiple choice");
        mcqTitle.getStyleClass().add("cv-section-title");
        quizContainer.getChildren().add(mcqTitle);

        for (int i = 0; i < mcqs.size(); i++) {
            quizContainer.getChildren().add(buildMCQCard(mcqs.get(i), i + 1));
        }

        // Section header: Fill in the blank
        Label fillTitle = new Label("Fill in the blank");
        fillTitle.getStyleClass().add("cv-section-title");
        VBox.setMargin(fillTitle, new Insets(20, 0, 0, 0));
        quizContainer.getChildren().add(fillTitle);

        for (int i = 0; i < fills.size(); i++) {
            quizContainer.getChildren().add(buildFillBlankCard(fills.get(i), i + 1));
        }
    }

    /**
     * Build an MC card from a Question whose content is formatted as:
     * "Question text\nA) option1\nB) option2\nC) option3\nD) option4"
     * and whose solution is the correct letter (e.g. "A", "B", "C", "D").
     */
    private VBox buildMCQCard(Question q, int number) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cv-mcq-card");
        card.setPadding(new Insets(18, 22, 18, 22));

        // Parse content into question text + options
        String[] contentLines = q.getContent().split("\n");
        String questionText = contentLines[0];
        List<String> options = new ArrayList<>();
        for (int i = 1; i < contentLines.length; i++) {
            // Strip leading "A) ", "B) ", etc.
            String opt = contentLines[i].replaceFirst("^[A-D]\\)\\s*", "");
            options.add(opt);
        }

        // Determine correct index from solution letter
        int correctIndex = 0;
        if (q.getSolution() != null && !q.getSolution().isEmpty()) {
            char letter = q.getSolution().charAt(0);
            correctIndex = letter - 'A';
        }
        final int correctIdx = correctIndex;

        Label qLabel = new Label("Q" + number + ". " + questionText);
        qLabel.getStyleClass().add("cv-mcq-question");
        qLabel.setWrapText(true);
        card.getChildren().add(qLabel);

        Label feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("cv-mcq-feedback");
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        VBox optionsBox = new VBox(8);
        String[] letters = { "A", "B", "C", "D" };

        for (int i = 0; i < options.size(); i++) {
            int idx = i;
            Button optBtn = new Button(letters[i] + ".  " + options.get(i));
            optBtn.getStyleClass().add("cv-mcq-option");
            optBtn.setMaxWidth(Double.MAX_VALUE);
            optBtn.setAlignment(Pos.CENTER_LEFT);

            optBtn.setOnAction(e -> {
                // Disable all options
                for (var child : optionsBox.getChildren()) {
                    child.setDisable(true);
                }

                if (idx == correctIdx) {
                    optBtn.getStyleClass().add("cv-mcq-correct");
                    feedbackLabel.setText("Correct!");
                    feedbackLabel.getStyleClass().add("cv-feedback-correct");
                } else {
                    optBtn.getStyleClass().add("cv-mcq-wrong");
                    // Highlight the correct one
                    Button correctBtn = (Button) optionsBox.getChildren().get(correctIdx);
                    correctBtn.getStyleClass().add("cv-mcq-correct");
                    feedbackLabel.setText("Wrong. The answer is " + letters[correctIdx] + ".");
                    feedbackLabel.getStyleClass().add("cv-feedback-wrong");
                }
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            });

            // hover effect
            optBtn.setOnMouseEntered(ev -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), optBtn);
                st.setToX(1.01);
                st.setToY(1.01);
                st.play();
            });
            optBtn.setOnMouseExited(ev -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), optBtn);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            optionsBox.getChildren().add(optBtn);
        }

        card.getChildren().addAll(optionsBox, feedbackLabel);
        return card;
    }

    /**
     * Build a fill-in-the-blank card from a Question:
     * - content = code template with ___
     * - solution = the expected answer
     * - hint = the hint text
     */
    private VBox buildFillBlankCard(Question q, int number) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cv-fill-card");
        card.setPadding(new Insets(18, 22, 18, 22));

        Label title = new Label("Exercise " + number);
        title.getStyleClass().add("cv-fill-title");

        // Code template with placeholder
        VBox codeBox = new VBox();
        codeBox.getStyleClass().add("cv-code-block");
        codeBox.setPadding(new Insets(12, 16, 12, 16));
        Label codeLabel = new Label(q.getContent());
        codeLabel.getStyleClass().add("cv-code-text");
        codeLabel.setWrapText(true);
        codeBox.getChildren().add(codeLabel);

        Label hintLabel = new Label("Hint: " + (q.getHint() != null ? q.getHint() : ""));
        hintLabel.getStyleClass().add("cv-fill-hint");
        hintLabel.setWrapText(true);

        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        TextField inputField = new TextField();
        inputField.setPromptText("Type your answer…");
        inputField.getStyleClass().add("cv-fill-input");
        inputField.setPrefWidth(240);

        Button checkBtn = new Button("Check");
        checkBtn.getStyleClass().add("cv-fill-check-btn");

        Label resultLabel = new Label();
        resultLabel.getStyleClass().add("cv-fill-result");

        checkBtn.setOnAction(e -> {
            String userAnswer = inputField.getText().trim();
            if (userAnswer.equalsIgnoreCase(q.getSolution())) {
                resultLabel.setText("Correct!");
                resultLabel.getStyleClass().removeAll("cv-feedback-wrong");
                resultLabel.getStyleClass().add("cv-feedback-correct");
                inputField.setDisable(true);
                checkBtn.setDisable(true);
            } else {
                resultLabel.setText("Try again. Expected: " + q.getSolution());
                resultLabel.getStyleClass().removeAll("cv-feedback-correct");
                resultLabel.getStyleClass().add("cv-feedback-wrong");
            }
        });

        inputRow.getChildren().addAll(inputField, checkBtn);
        card.getChildren().addAll(title, codeBox, hintLabel, inputRow, resultLabel);
        return card;
    }

    // ── Practice section ──────────────────────────────────────────────

    /**
     * Build the practice section from CODE_IMPLEMENTATION questions.
     * - content = description + starter code (separated by double newline)
     * - solution = expected output
     * - hint = coding hint
     */
    private void buildPracticeSection(List<Question> codeQuestions) {
        practiceContainer.getChildren().clear();
        if (codeQuestions == null || codeQuestions.isEmpty())
            return;

        for (int i = 0; i < codeQuestions.size(); i++) {
            Question task = codeQuestions.get(i);

            Label titleLabel = new Label("Programming task " + (i + 1));
            titleLabel.getStyleClass().add("cv-section-title");

            // Split content into description and starter code at the first code block
            String taskContent = task.getContent();
            String description = taskContent;
            String starterCode = "";
            int codeBlockStart = taskContent.indexOf("\n\n");
            if (codeBlockStart >= 0) {
                description = taskContent.substring(0, codeBlockStart);
                starterCode = taskContent.substring(codeBlockStart + 2);
            }

            Label desc = new Label(description);
            desc.getStyleClass().add("cv-learn-text");
            desc.setWrapText(true);

            Label starterTitle = new Label("Starter Code:");
            starterTitle.getStyleClass().add("cv-practice-subtitle");

            VBox starterBox = new VBox();
            starterBox.getStyleClass().add("cv-code-block");
            starterBox.setPadding(new Insets(14, 18, 14, 18));
            Label starterLabel = new Label(starterCode);
            starterLabel.getStyleClass().add("cv-code-text");
            starterLabel.setWrapText(true);
            starterBox.getChildren().add(starterLabel);

            // Editable text area for user code
            Label yourCodeTitle = new Label("Your Solution:");
            yourCodeTitle.getStyleClass().add("cv-practice-subtitle");

            TextArea codeArea = new TextArea(starterCode);
            codeArea.getStyleClass().add("cv-code-area");
            codeArea.setPrefHeight(200);
            codeArea.setWrapText(true);

            Label expectedTitle = new Label("Expected Output:");
            expectedTitle.getStyleClass().add("cv-practice-subtitle");

            VBox expectedBox = new VBox();
            expectedBox.getStyleClass().add("cv-expected-output");
            expectedBox.setPadding(new Insets(12, 16, 12, 16));
            Label expectedLabel = new Label(task.getSolution());
            expectedLabel.getStyleClass().add("cv-expected-text");
            expectedLabel.setWrapText(true);
            expectedBox.getChildren().add(expectedLabel);

            // Submit button (placeholder)
            Button runBtn = new Button("Run code");
            runBtn.getStyleClass().add("cv-run-btn");

            Label runResult = new Label();
            runResult.getStyleClass().add("cv-run-result");

            runBtn.setOnAction(e -> {
                runResult.setText("Code submitted successfully. (Evaluation is simulated)");
                runResult.getStyleClass().add("cv-feedback-correct");
            });

            practiceContainer.getChildren().addAll(
                    titleLabel, desc, starterTitle, starterBox,
                    yourCodeTitle, codeArea,
                    expectedTitle, expectedBox,
                    runBtn, runResult);
        }
    }
}

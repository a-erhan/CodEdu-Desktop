package com.codedu.controllers;

import com.codedu.models.Chapter;
import com.codedu.models.ChapterContent;
import com.codedu.models.ChapterContent.*;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

/**
 * Controller for the interactive chapter content view.
 * Renders Learn / Quiz / Practice sections for a given chapter.
 */
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
        headerXP.setText("⭐ " + chapter.getXpReward() + " XP");

        ChapterContent content = chapter.getContent();
        if (content != null) {
            buildLearnSection(content.getLearnText());
            buildQuizSection(content.getMcQuestions(), content.getFillBlanks());
            buildPracticeSection(content.getProgrammingTask());
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

    private void buildQuizSection(List<MCQuestion> mcqs, List<FillBlank> fills) {
        quizContainer.getChildren().clear();

        // Section header: MCQ
        Label mcqTitle = new Label("📝  Multiple Choice");
        mcqTitle.getStyleClass().add("cv-section-title");
        quizContainer.getChildren().add(mcqTitle);

        for (int i = 0; i < mcqs.size(); i++) {
            quizContainer.getChildren().add(buildMCQCard(mcqs.get(i), i + 1));
        }

        // Section header: Fill in the blank
        Label fillTitle = new Label("✏️  Fill in the Blank");
        fillTitle.getStyleClass().add("cv-section-title");
        VBox.setMargin(fillTitle, new Insets(20, 0, 0, 0));
        quizContainer.getChildren().add(fillTitle);

        for (int i = 0; i < fills.size(); i++) {
            quizContainer.getChildren().add(buildFillBlankCard(fills.get(i), i + 1));
        }
    }

    private VBox buildMCQCard(MCQuestion q, int number) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cv-mcq-card");
        card.setPadding(new Insets(18, 22, 18, 22));

        Label qLabel = new Label("Q" + number + ". " + q.getQuestion());
        qLabel.getStyleClass().add("cv-mcq-question");
        qLabel.setWrapText(true);
        card.getChildren().add(qLabel);

        Label feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("cv-mcq-feedback");
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        VBox optionsBox = new VBox(8);
        String[] letters = { "A", "B", "C", "D" };

        for (int i = 0; i < q.getOptions().size(); i++) {
            int idx = i;
            Button optBtn = new Button(letters[i] + ".  " + q.getOptions().get(i));
            optBtn.getStyleClass().add("cv-mcq-option");
            optBtn.setMaxWidth(Double.MAX_VALUE);
            optBtn.setAlignment(Pos.CENTER_LEFT);

            optBtn.setOnAction(e -> {
                // Disable all options
                for (var child : optionsBox.getChildren()) {
                    child.setDisable(true);
                }

                if (idx == q.getCorrectIndex()) {
                    optBtn.getStyleClass().add("cv-mcq-correct");
                    feedbackLabel.setText("✅  Correct!");
                    feedbackLabel.getStyleClass().add("cv-feedback-correct");
                } else {
                    optBtn.getStyleClass().add("cv-mcq-wrong");
                    // Highlight the correct one
                    Button correctBtn = (Button) optionsBox.getChildren().get(q.getCorrectIndex());
                    correctBtn.getStyleClass().add("cv-mcq-correct");
                    feedbackLabel.setText("❌  Wrong! The answer is " + letters[q.getCorrectIndex()] + ".");
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

    private VBox buildFillBlankCard(FillBlank fb, int number) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cv-fill-card");
        card.setPadding(new Insets(18, 22, 18, 22));

        Label title = new Label("Exercise " + number);
        title.getStyleClass().add("cv-fill-title");

        // Code template with placeholder
        VBox codeBox = new VBox();
        codeBox.getStyleClass().add("cv-code-block");
        codeBox.setPadding(new Insets(12, 16, 12, 16));
        Label codeLabel = new Label(fb.getCodeTemplate());
        codeLabel.getStyleClass().add("cv-code-text");
        codeLabel.setWrapText(true);
        codeBox.getChildren().add(codeLabel);

        Label hintLabel = new Label("💡 Hint: " + fb.getHint());
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
            if (userAnswer.equalsIgnoreCase(fb.getAnswer())) {
                resultLabel.setText("✅  Correct!");
                resultLabel.getStyleClass().removeAll("cv-feedback-wrong");
                resultLabel.getStyleClass().add("cv-feedback-correct");
                inputField.setDisable(true);
                checkBtn.setDisable(true);
            } else {
                resultLabel.setText("❌  Try again! Expected: " + fb.getAnswer());
                resultLabel.getStyleClass().removeAll("cv-feedback-correct");
                resultLabel.getStyleClass().add("cv-feedback-wrong");
            }
        });

        inputRow.getChildren().addAll(inputField, checkBtn);
        card.getChildren().addAll(title, codeBox, hintLabel, inputRow, resultLabel);
        return card;
    }

    // ── Practice section ──────────────────────────────────────────────

    private void buildPracticeSection(ProgrammingTask task) {
        practiceContainer.getChildren().clear();
        if (task == null)
            return;

        Label title = new Label("🚀  Programming Task");
        title.getStyleClass().add("cv-section-title");

        Label desc = new Label(task.getDescription());
        desc.getStyleClass().add("cv-learn-text");
        desc.setWrapText(true);

        Label starterTitle = new Label("Starter Code:");
        starterTitle.getStyleClass().add("cv-practice-subtitle");

        VBox starterBox = new VBox();
        starterBox.getStyleClass().add("cv-code-block");
        starterBox.setPadding(new Insets(14, 18, 14, 18));
        Label starterLabel = new Label(task.getStarterCode());
        starterLabel.getStyleClass().add("cv-code-text");
        starterLabel.setWrapText(true);
        starterBox.getChildren().add(starterLabel);

        // Editable text area for user code
        Label yourCodeTitle = new Label("Your Solution:");
        yourCodeTitle.getStyleClass().add("cv-practice-subtitle");

        TextArea codeArea = new TextArea(task.getStarterCode());
        codeArea.getStyleClass().add("cv-code-area");
        codeArea.setPrefHeight(200);
        codeArea.setWrapText(true);

        Label expectedTitle = new Label("Expected Output:");
        expectedTitle.getStyleClass().add("cv-practice-subtitle");

        VBox expectedBox = new VBox();
        expectedBox.getStyleClass().add("cv-expected-output");
        expectedBox.setPadding(new Insets(12, 16, 12, 16));
        Label expectedLabel = new Label(task.getExpectedOutput());
        expectedLabel.getStyleClass().add("cv-expected-text");
        expectedLabel.setWrapText(true);
        expectedBox.getChildren().add(expectedLabel);

        // Submit button (placeholder)
        Button runBtn = new Button("▶  Run Code");
        runBtn.getStyleClass().add("cv-run-btn");

        Label runResult = new Label();
        runResult.getStyleClass().add("cv-run-result");

        runBtn.setOnAction(e -> {
            runResult.setText("✅  Code submitted successfully! (Evaluation is simulated)");
            runResult.getStyleClass().add("cv-feedback-correct");
        });

        practiceContainer.getChildren().addAll(
                title, desc, starterTitle, starterBox,
                yourCodeTitle, codeArea,
                expectedTitle, expectedBox,
                runBtn, runResult);
    }
}

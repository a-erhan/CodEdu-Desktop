package com.codedu.controllers;

import com.codedu.models.learning.*;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.services.interfaces.UserChapterProgressService;
import com.codedu.services.interfaces.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChapterViewController {

    @Autowired
    private UserChapterProgressService progressService;

    @Autowired
    private UserService userService;

    @FXML private Button btnBack;
    @FXML private Label headerTitle;
    @FXML private Label headerXP;
    @FXML private Button tabLearn, tabQuiz, tabPractice;
    @FXML private VBox learnContainer, quizContainer, practiceContainer;
    @FXML private ScrollPane learnScroll, quizScroll, practiceScroll;

    private Chapter chapter;
    private UserChapterProgress userProgress;
    private Runnable onBack;
    private boolean isChapterFinished = false;
    private List<Question> uiQuestionOrder = new ArrayList<>();

    // 🚀 THE FIX: A local counter to guarantee the screen updates instantly
    private int currentLessonCount = 0;

    @FXML
    public void initialize() {
        tabLearn.setOnAction(e -> switchTab("learn"));
        tabQuiz.setOnAction(e -> switchTab("quiz"));
        tabPractice.setOnAction(e -> switchTab("practice"));
    }

    public void setChapter(Chapter chapter, UserChapterProgress progress) {
        this.chapter = chapter;
        this.userProgress = progress;
        this.isChapterFinished = (progress != null && progress.isCompleted());

        // 🚀 Initialize our local counter
        this.currentLessonCount = (progress != null) ? progress.getCompletedLessons() : 0;

        headerTitle.setText(chapter.getTitle());
        updateHeaderProgress();

        if (chapter.getIconImage() != null) {
            try {
                Image img = new Image(getClass().getResourceAsStream(chapter.getIconImage()));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(28); iv.setFitHeight(28); iv.setPreserveRatio(true);
                headerTitle.setGraphic(iv);
            } catch (Exception e) { /* Fallback */ }
        }

        ChapterContent content = chapter.getContent();
        if (content != null) {
            buildLearnSection(content.getLearnText());

            List<Question> questions = content.getQuestions() != null ? content.getQuestions() : new ArrayList<>();

            List<Question> mcQuestions = questions.stream()
                    .filter(q -> q.getQuestionType() == QuestionType.MULTIPLE_CHOICES)
                    .collect(Collectors.toList());

            List<Question> fillBlanks = questions.stream()
                    .filter(q -> q.getQuestionType() == QuestionType.FILL_IN_THE_BLANKS)
                    .collect(Collectors.toList());

            List<Question> codeQuestions = questions.stream()
                    .filter(q -> q.getQuestionType() == QuestionType.CODE_IMPLEMENTATION)
                    .collect(Collectors.toList());

            uiQuestionOrder.clear();
            uiQuestionOrder.addAll(mcQuestions);
            uiQuestionOrder.addAll(fillBlanks);
            uiQuestionOrder.addAll(codeQuestions);

            buildQuizSection(mcQuestions, fillBlanks);
            buildPracticeSection(codeQuestions);
        }
    }

    private void switchTab(String tab) {
        learnScroll.setVisible("learn".equals(tab));
        quizScroll.setVisible("quiz".equals(tab));
        practiceScroll.setVisible("practice".equals(tab));

        tabLearn.getStyleClass().remove("cv-tab-active");
        tabQuiz.getStyleClass().remove("cv-tab-active");
        tabPractice.getStyleClass().remove("cv-tab-active");

        if ("learn".equals(tab)) tabLearn.getStyleClass().add("cv-tab-active");
        else if ("quiz".equals(tab)) tabQuiz.getStyleClass().add("cv-tab-active");
        else if ("practice".equals(tab)) tabPractice.getStyleClass().add("cv-tab-active");
    }

    private void buildLearnSection(String text) {
        learnContainer.getChildren().clear();
        if (text == null || text.isEmpty()) text = "No learning material available for this chapter yet.";
        Label learnLabel = new Label(text);
        learnLabel.setWrapText(true);
        learnLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20px; -fx-line-spacing: 0.5em; -fx-text-fill: #FFFFFF;");
        learnLabel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(learnLabel, Priority.ALWAYS);
        learnContainer.getChildren().add(learnLabel);
    }

    private void buildQuizSection(List<Question> mcqs, List<Question> fills) {
        quizContainer.getChildren().clear();
        Label mcqHeader = new Label("Multiple Choice");
        mcqHeader.getStyleClass().add("cv-section-title");
        quizContainer.getChildren().add(mcqHeader);
        for (int i = 0; i < mcqs.size(); i++) quizContainer.getChildren().add(buildMCQCard(mcqs.get(i), i + 1));

        Label fillHeader = new Label("Fill in the Blank");
        fillHeader.getStyleClass().add("cv-section-title");
        VBox.setMargin(fillHeader, new Insets(20, 0, 0, 0));
        quizContainer.getChildren().add(fillHeader);
        for (int i = 0; i < fills.size(); i++) quizContainer.getChildren().add(buildFillBlankCard(fills.get(i), i + 1));
    }

    private VBox buildMCQCard(Question q, int number) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cv-mcq-card");
        card.setPadding(new Insets(18, 22, 18, 22));

        String[] contentLines = q.getContent().split("\n");
        List<String> options = new ArrayList<>();
        for (int i = 1; i < contentLines.length; i++) options.add(contentLines[i].replaceFirst("^[A-D]\\)\\s*", ""));

        int correctIndex = (q.getSolution() != null && !q.getSolution().isEmpty()) ? q.getSolution().toUpperCase().charAt(0) - 'A' : 0;

        Label qLabel = new Label("Q" + number + ". " + contentLines[0]);
        qLabel.getStyleClass().add("cv-mcq-question");
        card.getChildren().add(qLabel);

        VBox optionsBox = new VBox(8);
        String[] letters = {"A", "B", "C", "D"};

        boolean isLocked = isQuestionCompleted(q);
        String styleCorrect = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #27ae60; -fx-border-radius: 4; -fx-background-radius: 4;";
        String styleWrong = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #c0392b; -fx-border-radius: 4; -fx-background-radius: 4;";

        for (int i = 0; i < options.size(); i++) {
            int idx = i;
            Button optBtn = new Button(letters[i] + ".  " + options.get(i));
            optBtn.getStyleClass().add("cv-mcq-option");
            optBtn.setMaxWidth(Double.MAX_VALUE);

            if (isLocked) {
                optBtn.setMouseTransparent(true);
                if (idx == correctIndex) optBtn.setStyle(styleCorrect);
            }

            optBtn.setOnAction(e -> {
                if (idx == correctIndex) {
                    optBtn.setStyle(styleCorrect);
                    handleCorrectAnswer(q);
                } else {
                    optBtn.setStyle(styleWrong);
                    if (optionsBox.getChildren().get(correctIndex) instanceof Button correctBtn) {
                        correctBtn.setStyle(styleCorrect);
                    }
                }
                optionsBox.getChildren().forEach(c -> c.setMouseTransparent(true));
            });
            optionsBox.getChildren().add(optBtn);
        }
        card.getChildren().add(optionsBox);
        return card;
    }

    private VBox buildFillBlankCard(Question q, int number) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cv-fill-card");
        card.setPadding(new Insets(18, 22, 18, 22));

        Label title = new Label("Exercise " + number);
        Label codeLabel = new Label(q.getContent());
        TextField inputField = new TextField();
        Button checkBtn = new Button("Check");

        String styleCorrectField = "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #28a745;";

        if (isQuestionCompleted(q)) {
            inputField.setText(q.getSolution());
            inputField.setStyle(styleCorrectField);
            inputField.setMouseTransparent(true);
            checkBtn.setMouseTransparent(true);
        }

        checkBtn.setOnAction(e -> {
            if (inputField.getText().trim().equalsIgnoreCase(q.getSolution())) {
                inputField.setStyle(styleCorrectField);
                inputField.setMouseTransparent(true);
                checkBtn.setMouseTransparent(true);
                handleCorrectAnswer(q);
            } else {
                inputField.setStyle("-fx-border-color: #dc3545;");
            }
        });

        card.getChildren().addAll(title, codeLabel, new HBox(10, inputField, checkBtn));
        return card;
    }

    private void buildPracticeSection(List<Question> codeQuestions) {
        practiceContainer.getChildren().clear();
        for (int i = 0; i < codeQuestions.size(); i++) {
            Question task = codeQuestions.get(i);
            TextArea codeArea = new TextArea(task.getContent());
            Button runBtn = new Button("Run Code");

            if (isQuestionCompleted(task)) {
                codeArea.setEditable(false);
                runBtn.setVisible(false);
            }

            runBtn.setOnAction(e -> handleCorrectAnswer(task));
            practiceContainer.getChildren().addAll(new Label(task.getTitle()), codeArea, runBtn);
        }
    }

    /**
     * Completely Overhauled "UI-First" Progress Logic
     */
    /**
     * Completely Overhauled "UI-First" Progress Logic
     */
    private void handleCorrectAnswer(Question q) {
        // 🚀 1. UPDATE THE UI STATE FIRST (Guarantees the user sees progress)
        currentLessonCount++;

        if (chapter != null && currentLessonCount >= chapter.getTotalLessons()) {
            isChapterFinished = true;
            if (userProgress != null) userProgress.setCompleted(true);
        }

        // 🚀 2. ATTEMPT DATABASE SAVES IN THE BACKGROUND
        try {
            if (userProgress != null && userProgress.getUser() != null) {

                // 🚀 THE FIX: Pull the REAL record from Neon so we update it instead of making duplicates!
                UserChapterProgress realProgress = progressService.getProgress(userProgress.getUser(), chapter);

                // Safety fallback just in case
                if (realProgress == null) realProgress = userProgress;

                realProgress.setCompletedLessons(currentLessonCount);
                if (isChapterFinished) realProgress.setCompleted(true);

                // Now it will cleanly UPDATE the existing row!
                progressService.saveProgress(realProgress);

                // Fetch a FRESH user to avoid the "Detached" database crash for XP!
                userService.getUserWithProfileData(userProgress.getUser().getUsername())
                        .ifPresent(freshUser -> {
                            UserGameState state = freshUser.getGameState();
                            if (state != null && q != null && q.getReward() != null) {
                                state.setXp(state.getXp() + q.getReward().getXp());
                                state.setTokenBalance(state.getTokenBalance() + q.getReward().getToken());
                                userService.saveUser(freshUser); // Save the updated fresh user!
                            }
                        });
            }
        } catch (Exception e) {
            System.err.println("⚠️ Database save issue, but UI will still update.");
            e.printStackTrace();
        } finally {
            // 🚀 3. PUSH UPDATED COUNTER TO THE SCREEN INSTANTLY
            javafx.application.Platform.runLater(this::updateHeaderProgress);
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
        btnBack.setOnAction(e -> { if (onBack != null) onBack.run(); });
    }

    private void updateHeaderProgress() {
        int total = (chapter != null) ? chapter.getTotalLessons() : 0;
        int xpReward = (chapter != null) ? chapter.getXpReward() : 0;

        // 🚀 Uses our safe local counter instead of the database object
        headerXP.setText(String.format("XP: %d | %d/%d Lessons", xpReward, currentLessonCount, total));
    }

    private boolean isQuestionCompleted(Question q) {
        if (isChapterFinished) return true;

        int globalIndex = uiQuestionOrder.indexOf(q);
        if (globalIndex == -1) {
            for (int i = 0; i < uiQuestionOrder.size(); i++) {
                if (uiQuestionOrder.get(i).getId() == q.getId()) {
                    globalIndex = i;
                    break;
                }
            }
        }

        // 🚀 Checks against the safe local counter
        return (globalIndex != -1 && globalIndex < currentLessonCount);
    }
}
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
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChapterViewController {

    @Autowired
    private UserChapterProgressService progressService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Button btnBack;
    @FXML
    private Label headerTitle;
    @FXML
    private Label headerXP;
    @FXML
    private Button tabLearn, tabQuiz, tabPractice;
    @FXML
    private VBox learnContainer, quizContainer, practiceContainer;
    @FXML
    private ScrollPane learnScroll, quizScroll, practiceScroll;

    private Chapter chapter;
    private UserChapterProgress userProgress;
    private Runnable onBack;
    private boolean isChapterFinished = false;
    private List<Question> uiQuestionOrder = new ArrayList<>();

    private int currentLessonCount = 0;
    private java.util.function.Consumer<User> onProgressUpdated;


    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

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

        this.currentLessonCount = (progress != null) ? progress.getCompletedLessons() : 0;

        headerTitle.setText(chapter.getTitle());

        if (chapter.getIconImage() != null) {
            try {
                Image img = new Image(getClass().getResourceAsStream(chapter.getIconImage()));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(28);
                iv.setFitHeight(28);
                iv.setPreserveRatio(true);
                headerTitle.setGraphic(iv);
            } catch (Exception e) {
                /* Fallback */
            }
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

        // 🚀 CRITICAL: Update header at the VERY END after uiQuestionOrder is populated!
        updateHeaderProgress();
    }

    private void switchTab(String tab) {
        learnScroll.setVisible("learn".equals(tab));
        quizScroll.setVisible("quiz".equals(tab));
        practiceScroll.setVisible("practice".equals(tab));

        tabLearn.getStyleClass().remove("cv-tab-active");
        tabQuiz.getStyleClass().remove("cv-tab-active");
        tabPractice.getStyleClass().remove("cv-tab-active");

        if ("learn".equals(tab))
            tabLearn.getStyleClass().add("cv-tab-active");
        else if ("quiz".equals(tab))
            tabQuiz.getStyleClass().add("cv-tab-active");
        else if ("practice".equals(tab))
            tabPractice.getStyleClass().add("cv-tab-active");
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
        for (int i = 0; i < mcqs.size(); i++)
            quizContainer.getChildren().add(buildMCQCard(mcqs.get(i), i + 1));

        Label fillHeader = new Label("Fill in the Blank");
        fillHeader.getStyleClass().add("cv-section-title");
        VBox.setMargin(fillHeader, new Insets(20, 0, 0, 0));
        quizContainer.getChildren().add(fillHeader);
        for (int i = 0; i < fills.size(); i++)
            quizContainer.getChildren().add(buildFillBlankCard(fills.get(i), i + 1));
    }

    private VBox buildMCQCard(Question q, int number) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cv-mcq-card");
        card.setPadding(new Insets(18, 22, 18, 22));

        String[] contentLines = q.getContent().split("\n");
        List<String> options = new ArrayList<>();
        for (int i = 1; i < contentLines.length; i++)
            options.add(contentLines[i].replaceFirst("^[A-D]\\)\\s*", ""));

        int correctIndex = (q.getSolution() != null && !q.getSolution().isEmpty()) ? q.getSolution().toUpperCase().charAt(0) - 'A' : 0;

        Label qLabel = new Label("Q" + number + ". " + contentLines[0]);
        qLabel.getStyleClass().add("cv-mcq-question");
        card.getChildren().add(qLabel);

        VBox optionsBox = new VBox(8);
        String[] letters = { "A", "B", "C", "D" };

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
                if (idx == correctIndex)
                    optBtn.setStyle(styleCorrect);
            }

            optBtn.setOnAction(e -> {
                if (!hasEnoughHearts()) return;
                if (idx == correctIndex) {
                    optBtn.setStyle(styleCorrect);
                    handleCorrectAnswer(q);
                } else {
                    optBtn.setStyle(styleWrong);
                    if (optionsBox.getChildren().get(correctIndex) instanceof Button correctBtn) {
                        correctBtn.setStyle(styleCorrect);
                    }
                    handleWrongAnswer();
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
            if (!hasEnoughHearts()) return;
            if (inputField.getText().trim().equalsIgnoreCase(q.getSolution())) {
                inputField.setStyle(styleCorrectField);
                inputField.setMouseTransparent(true);
                checkBtn.setMouseTransparent(true);
                handleCorrectAnswer(q);
            } else {
                inputField.setStyle("-fx-border-color: #dc3545;");
                handleWrongAnswer();
            }
        });

        card.getChildren().addAll(title, codeLabel, new HBox(10, inputField, checkBtn));
        return card;
    }

    private void buildPracticeSection(List<Question> codeQuestions) {
        practiceContainer.getChildren().clear();

        if (codeQuestions.isEmpty()) {
            Label emptyLabel = new Label("No coding exercises for this chapter yet.");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-padding: 20px;");
            practiceContainer.getChildren().add(emptyLabel);
            return;
        }

        for (int i = 0; i < codeQuestions.size(); i++) {
            Question task = codeQuestions.get(i);

            try {
                // 1. Load your specialized QuestionSolver FXML
                // ⚠️ Ensure this path matches exactly where your FXML is saved!
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/com/codedu/views/QuestionSolver.fxml")
                );

                // 2. Let Spring wire up the JDoodle Service inside the controller
                loader.setControllerFactory(applicationContext::getBean);

                // 3. Load the UI node (assuming the root of QuestionSolver.fxml is a VBox)
                javafx.scene.Node solverUI = loader.load();

                // 4. Get the controller and pass the question data
                QuestionSolverController solverController = loader.getController();
                solverController.setQuestion(task);

                if (isQuestionCompleted(task)) {
                    solverController.setLocked(true); // Lock it immediately!
                }


                solverController.setOnSuccessCallback(isCorrect -> {
                    if (isCorrect) {
                        // Only award XP if they haven't beaten this exact question before
                        if (!isQuestionCompleted(task)) {
                            handleCorrectAnswer(task);
                            System.out.println("✅ Practice question completed, XP awarded!");
                        }
                    }
                });

                solverController.setHeartCheckCallback(this::hasEnoughHearts);
                solverController.setOnWrongAnswerCallback(this::handleWrongAnswer);

                // Optional: Add some spacing between multiple code questions
                VBox.setMargin(solverUI, new Insets(0, 0, 30, 0));
                practiceContainer.getChildren().add(solverUI);

            } catch (Exception e) {
                e.printStackTrace();
                Label errorLabel = new Label("⚠️ Failed to load code editor for: " + task.getTitle());
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                practiceContainer.getChildren().add(errorLabel);
            }
        }
    }

    private void handleCorrectAnswer(Question q) {
        // Check if the chapter was already finished before this answer
        boolean alreadyFinishedBefore = isChapterFinished;

        currentLessonCount++;
        int dynamicTotalLessons = (uiQuestionOrder != null) ? uiQuestionOrder.size() : 0;

        if (chapter != null && currentLessonCount >= dynamicTotalLessons) {
            isChapterFinished = true;
            if (userProgress != null) userProgress.setCompleted(true);
        }

        try {
            if (currentUser != null) {
                // Save chapter progress
                UserChapterProgress realProgress = progressService.getProgress(currentUser, chapter);
                if (realProgress == null) realProgress = userProgress;

                if (realProgress != null) {
                    realProgress.setCompletedLessons(currentLessonCount);
                    if (isChapterFinished) realProgress.setCompleted(true);
                    progressService.saveProgress(realProgress);
                }

                // Calculate standard rewards from the question
                int xpReward = (q != null && q.getReward() != null) ? q.getReward().getXp() : 0;
                int tokenReward = (q != null && q.getReward() != null) ? q.getReward().getToken() : 0;


                if (isChapterFinished && !alreadyFinishedBefore && chapter != null) {
                    xpReward += chapter.getXpReward(); // Add the large chapter completion bonus
                    tokenReward += 50; // Optional: Add a flat token bonus for finishing chapters
                }

                if (xpReward > 0 || tokenReward > 0) {
                    User updatedUser = userService.awardXpAndTokens(currentUser.getUsername(), xpReward, tokenReward);


                    if (updatedUser != null && onProgressUpdated != null) {
                        javafx.application.Platform.runLater(() -> onProgressUpdated.accept(updatedUser));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Database save issue, but UI will still update.");
            e.printStackTrace();
        } finally {
            // 3. PUSH UPDATED COUNTER TO THE SCREEN INSTANTLY
            javafx.application.Platform.runLater(this::updateHeaderProgress);
        }
    }

    private void updateHeaderProgress() {

        int total = uiQuestionOrder.size();


        if (total == 0 && chapter != null) {
            total = chapter.getTotalLessons();
        }

        int xpReward = (chapter != null) ? chapter.getXpReward() : 0;
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

        return (globalIndex != -1 && globalIndex < currentLessonCount);
    }

    public void setOnProgressUpdated(java.util.function.Consumer<User> onProgressUpdated) {
        this.onProgressUpdated = onProgressUpdated;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
        btnBack.setOnAction(e -> { if (onBack != null) onBack.run(); });
    }

    private boolean hasEnoughHearts() {
        if (currentUser != null && currentUser.getGameState() != null) {
            if (currentUser.getGameState().getHeartCount() <= 0) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Out of Hearts!");
                alert.setHeaderText(null);
                alert.setContentText("You have 0 hearts remaining. Wait for them to refill or buy more in the store!");
                alert.showAndWait();
                return false; // Blocks the code from running
            }
        }
        return true;
    }

    private void handleWrongAnswer() {
        if (currentUser != null) {

            userService.decrementHeart(currentUser.getUsername());

            userService.getUserWithProfileData(currentUser.getUsername()).ifPresent(freshUser -> {


                this.currentUser = freshUser;


                if (onProgressUpdated != null) {
                    javafx.application.Platform.runLater(() -> onProgressUpdated.accept(freshUser));
                }
            });
        }
    }
}
package com.codedu.controllers;

import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.dtos.learning.ChapterDTO;
import com.codedu.dtos.learning.QuestionDTO;
import com.codedu.models.learning.QuestionType;
import com.codedu.models.user.User;
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

    @Autowired private UserChapterProgressService progressService;
    @Autowired private UserService userService;
    @Autowired private ApplicationContext applicationContext;

    @FXML private Button btnBack;
    @FXML private Label headerTitle;
    @FXML private Label headerXP;
    @FXML private Button tabLearn, tabQuiz, tabPractice;
    @FXML private VBox learnContainer, quizContainer, practiceContainer;
    @FXML private ScrollPane learnScroll, quizScroll, practiceScroll;

    private ChapterDTO chapter;
    private ChapterProgressDTO userProgress;
    private Runnable onBack;
    private boolean isChapterFinished = false;
    private List<QuestionDTO> uiQuestionOrder = new ArrayList<>();

    private int currentLessonCount = 0;
    private java.util.function.Consumer<User> onProgressUpdated;
    private User currentUser;

    private final String LOGO_BLUE = "#00AEEF";
    private final String LOGO_ORANGE = "#F7941D";
    private final String CARD_BG = "#3b4252";
    private final String CARD_BORDER = "#4c566a";

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        tabLearn.setOnAction(e -> switchTab("learn"));
        tabQuiz.setOnAction(e -> switchTab("quiz"));
        tabPractice.setOnAction(e -> switchTab("practice"));

        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        headerTitle.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold; -fx-font-size: 20px;");
        headerXP.setStyle("-fx-text-fill: " + LOGO_BLUE + "; -fx-font-weight: bold;");
    }

    public void setChapter(ChapterDTO chapter, ChapterProgressDTO progress) {
        this.chapter = chapter;
        this.userProgress = progress;
        this.isChapterFinished = (progress != null && progress.isCompleted());
        this.currentLessonCount = (progress != null) ? progress.getCompletedLessons() : 0;

        headerTitle.setText(chapter.title());

        if (chapter.iconImage() != null) {
            try {
                Image img = new Image(getClass().getResourceAsStream(chapter.iconImage()));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(28); iv.setFitHeight(28); iv.setPreserveRatio(true);
                headerTitle.setGraphic(iv);
            } catch (Exception e) {}
        }

        buildLearnSection(chapter.learnText());

        List<QuestionDTO> questions = chapter.questions() != null ? chapter.questions() : new ArrayList<>();

        List<QuestionDTO> mcQuestions = questions.stream()
                .filter(q -> q.questionType() == QuestionType.MULTIPLE_CHOICES)
                .sorted(java.util.Comparator.comparingInt(this::getDifficultyWeight))
                .collect(Collectors.toList());

        List<QuestionDTO> fillBlanks = questions.stream()
                .filter(q -> q.questionType() == QuestionType.FILL_IN_THE_BLANKS)
                .sorted(java.util.Comparator.comparingInt(this::getDifficultyWeight))
                .collect(Collectors.toList());

        List<QuestionDTO> codeQuestions = questions.stream()
                .filter(q -> q.questionType() == QuestionType.CODE_IMPLEMENTATION)
                .sorted(java.util.Comparator.comparingInt(this::getDifficultyWeight))
                .collect(Collectors.toList());

        uiQuestionOrder.clear();
        uiQuestionOrder.addAll(mcQuestions);
        uiQuestionOrder.addAll(fillBlanks);
        uiQuestionOrder.addAll(codeQuestions);

        buildQuizSection(mcQuestions, fillBlanks);
        buildPracticeSection(codeQuestions);

        updateHeaderProgress();
    }

    private void switchTab(String tab) {
        learnScroll.setVisible("learn".equals(tab));
        quizScroll.setVisible("quiz".equals(tab));
        practiceScroll.setVisible("practice".equals(tab));

        tabLearn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d8dee9;");
        tabQuiz.setStyle("-fx-background-color: transparent; -fx-text-fill: #d8dee9;");
        tabPractice.setStyle("-fx-background-color: transparent; -fx-text-fill: #d8dee9;");

        String activeStyle = "-fx-background-color: transparent; -fx-border-color: " + LOGO_BLUE + "; -fx-border-width: 0 0 3 0; -fx-text-fill: " + LOGO_BLUE + "; -fx-font-weight: bold;";

        if ("learn".equals(tab)) tabLearn.setStyle(activeStyle);
        else if ("quiz".equals(tab)) tabQuiz.setStyle(activeStyle);
        else if ("practice".equals(tab)) tabPractice.setStyle(activeStyle);
    }

    private void buildLearnSection(String text) {
        learnContainer.getChildren().clear();
        if (text == null || text.isEmpty()) text = "No learning material available for this chapter yet.";

        VBox card = new VBox();
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 12; -fx-padding: 25;");

        Label learnLabel = new Label(text);
        learnLabel.setWrapText(true);
        learnLabel.setStyle("-fx-font-size: 16px; -fx-line-spacing: 0.6em; -fx-text-fill: #eceff4;");
        learnLabel.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().add(learnLabel);
        learnContainer.getChildren().add(card);
    }

    private void buildQuizSection(List<QuestionDTO> mcqs, List<QuestionDTO> fills) {
        quizContainer.getChildren().clear();

        Label mcqHeader = new Label("MULTIPLE CHOICE");
        mcqHeader.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold; -fx-letter-spacing: 0.1em;");
        quizContainer.getChildren().add(mcqHeader);

        for (int i = 0; i < mcqs.size(); i++) quizContainer.getChildren().add(buildMCQCard(mcqs.get(i), i + 1));

        Label fillHeader = new Label("FILL IN THE BLANK");
        fillHeader.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold; -fx-letter-spacing: 0.1em;");
        VBox.setMargin(fillHeader, new Insets(30, 0, 10, 0));
        quizContainer.getChildren().add(fillHeader);

        for (int i = 0; i < fills.size(); i++) quizContainer.getChildren().add(buildFillBlankCard(fills.get(i), i + 1));
    }

    private VBox buildMCQCard(QuestionDTO q, int number) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 12;");
        card.setPadding(new Insets(20));

        String[] contentLines = q.content().split("\n");
        List<String> options = new ArrayList<>();
        for (int i = 1; i < contentLines.length; i++) options.add(contentLines[i].replaceFirst("^[A-D]\\)\\s*", ""));

        int correctIndex = (q.solution() != null && !q.solution().isEmpty()) ? q.solution().toUpperCase().charAt(0) - 'A' : 0;

        Label qLabel = new Label("Q" + number + ". " + contentLines[0]);
        qLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        card.getChildren().add(buildMetadataHeader(q));
        card.getChildren().add(qLabel);

        VBox optionsBox = new VBox(10);
        String[] letters = { "A", "B", "C", "D" };

        boolean isLocked = isQuestionCompleted(q);
        String styleCorrect = "-fx-background-color: #a3be8c; -fx-text-fill: #2e3440; -fx-font-weight: bold; -fx-background-radius: 8;";
        String styleWrong = "-fx-background-color: #bf616a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;";
        String styleDefault = "-fx-background-color: transparent; -fx-border-color: #4c566a; -fx-border-radius: 8; -fx-text-fill: #d8dee9;";

        int[] wrongCount = {0};

        for (int i = 0; i < options.size(); i++) {
            int idx = i;
            Button optBtn = new Button(letters[i] + ".  " + options.get(i));
            optBtn.setStyle(styleDefault);
            optBtn.setMaxWidth(Double.MAX_VALUE);
            optBtn.setPadding(new Insets(10, 15, 10, 15));
            optBtn.setFocusTraversable(false);

            if (isLocked) {
                optBtn.setMouseTransparent(true);
                if (idx == correctIndex) optBtn.setStyle(styleCorrect);
            }

            optBtn.setOnAction(e -> {
                if (!hasEnoughHearts()) return;
                if (idx == correctIndex) {
                    optBtn.setStyle(styleCorrect);
                    optionsBox.getChildren().forEach(c -> c.setMouseTransparent(true));
                    handleCorrectAnswer(q, false);
                } else {
                    optBtn.setStyle(styleWrong);
                    optBtn.setMouseTransparent(true);
                    wrongCount[0]++;
                    handleWrongAnswer();

                    if (wrongCount[0] >= options.size() - 1) {
                        if (optionsBox.getChildren().get(correctIndex) instanceof Button correctBtn) {
                            correctBtn.setStyle(styleCorrect);
                        }
                        optionsBox.getChildren().forEach(c -> c.setMouseTransparent(true));
                        handleCorrectAnswer(q, true);
                    }
                }
            });
            optionsBox.getChildren().add(optBtn);
        }
        card.getChildren().add(optionsBox);
        return card;
    }

    private VBox buildFillBlankCard(QuestionDTO q, int number) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 12;");
        card.setPadding(new Insets(20));

        Label title = new Label("Exercise " + number);
        title.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold;");

        Label codeLabel = new Label(q.content());
        codeLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-text-fill: #eceff4; -fx-background-color: #2e3440; -fx-padding: 10; -fx-background-radius: 5;");

        TextField inputField = new TextField();
        inputField.setPromptText("Enter missing code...");
        inputField.setStyle("-fx-background-color: #434c5e; -fx-text-fill: white; -fx-border-color: #4c566a; -fx-border-radius: 4;");

        Button checkBtn = new Button("Check Answer");
        checkBtn.setStyle("-fx-background-color: " + LOGO_BLUE + "; -fx-text-fill: #2e3440; -fx-font-weight: bold;");

        String styleCorrectField = "-fx-background-color: #a3be8c; -fx-text-fill: #2e3440; -fx-border-color: #a3be8c;";

        if (isQuestionCompleted(q)) {
            inputField.setText(q.solution());
            inputField.setStyle(styleCorrectField);
            inputField.setMouseTransparent(true);
            checkBtn.setMouseTransparent(true);
            checkBtn.setOpacity(0.5);
        }

        checkBtn.setOnAction(e -> {
            if (!hasEnoughHearts()) return;
            String userInput = normalizeJavaCode(inputField.getText());
            String correctAnswer = normalizeJavaCode(q.solution());

            if (userInput.equalsIgnoreCase(correctAnswer)) {
                inputField.setStyle(styleCorrectField);
                inputField.setMouseTransparent(true);
                checkBtn.setMouseTransparent(true);
                handleCorrectAnswer(q,false);
            } else {
                inputField.setStyle("-fx-background-color: #434c5e; -fx-text-fill: white; -fx-border-color: #bf616a;");
                handleWrongAnswer();
            }
        });

        HBox controls = new HBox(10, inputField, checkBtn);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        card.getChildren().addAll(buildMetadataHeader(q), title, codeLabel, controls);
        return card;
    }

    private void buildPracticeSection(List<QuestionDTO> codeQuestions) {
        long quizCount = uiQuestionOrder.stream()
                .filter(q -> q.questionType() != QuestionType.CODE_IMPLEMENTATION)
                .count();

        boolean isGodMode = currentUser != null && currentUser.isTesterAccount();

        if (currentLessonCount < quizCount && !isChapterFinished && !isGodMode) {
            practiceContainer.getChildren().clear();
            VBox lockedUI = new VBox(20);
            lockedUI.setAlignment(javafx.geometry.Pos.CENTER);
            lockedUI.setPadding(new Insets(80, 40, 80, 40));

            Label lockIcon = new Label("🔒"); lockIcon.setStyle("-fx-font-size: 60px;");
            Label lockedTitle = new Label("Practice Section Locked");
            lockedTitle.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-size: 24px; -fx-font-weight: bold;");
            Label lockedDesc = new Label("Complete the theory and quiz sections to unlock this coding challenge!");
            lockedDesc.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 15px;");
            lockedDesc.setWrapText(true); lockedDesc.setMaxWidth(400);
            lockedDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            lockedUI.getChildren().addAll(lockIcon, lockedTitle, lockedDesc);
            practiceContainer.getChildren().add(lockedUI);
            return;
        }

        if (!practiceContainer.getChildren().isEmpty() &&
                practiceContainer.getChildren().get(0) instanceof VBox v &&
                !v.getChildren().isEmpty() &&
                v.getChildren().get(0) instanceof Label l && "🔒".equals(l.getText())) {
            practiceContainer.getChildren().clear();
        }

        if (codeQuestions.isEmpty()) {
            practiceContainer.getChildren().clear();
            Label emptyLabel = new Label("No coding exercises for this chapter yet.");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-padding: 20px;");
            practiceContainer.getChildren().add(emptyLabel);
            return;
        }

        for (int i = 0; i < codeQuestions.size(); i++) {
            QuestionDTO task = codeQuestions.get(i);

            boolean isAlreadyInUI = false;
            for (javafx.scene.Node node : practiceContainer.getChildren()) {
                if (node.getUserData() != null && node.getUserData().equals(task.id())) {
                    isAlreadyInUI = true;
                    break;
                }
            }

            if (!isAlreadyInUI) {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/codedu/views/QuestionSolver.fxml"));
                    loader.setControllerFactory(applicationContext::getBean);
                    javafx.scene.Node solverUI = loader.load();
                    solverUI.setFocusTraversable(false);

                    QuestionSolverController solverController = loader.getController();
                    solverController.setQuestion(task);

                    Runnable pinScroll = () -> {
                        double currentVval = practiceScroll.getVvalue();
                        practiceContainer.requestFocus();
                        javafx.application.Platform.runLater(() -> practiceScroll.setVvalue(currentVval));
                    };

                    solverController.setupGiveUpLogic(task.solution(), () -> {
                        pinScroll.run();
                        handleCorrectAnswer(task, true);
                        solverController.showSolutionState(task.solution());
                    });

                    if (isQuestionCompleted(task)) {
                        solverController.showSolutionState(task.solution());
                        solverController.setLocked(true);
                    }

                    Label hintLabel = new Label("💡 Hint: " + (task.hint() != null ? task.hint() : "Check your logic!"));
                    hintLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-padding: 12; -fx-background-color: rgba(247, 148, 29, 0.1); -fx-background-radius: 8;");
                    hintLabel.setWrapText(true); hintLabel.setVisible(false); hintLabel.setManaged(false);

                    solverController.setOnSuccessCallback(isCorrect -> {
                        if (isCorrect && !isQuestionCompleted(task)) {
                            pinScroll.run();
                            handleCorrectAnswer(task, false);
                            hintLabel.setVisible(false); hintLabel.setManaged(false);
                        }
                    });

                    solverController.setHeartCheckCallback(this::hasEnoughHearts);
                    solverController.setOnWrongAnswerCallback(() -> {
                        pinScroll.run();
                        handleWrongAnswer();
                        if (hasEnoughHearts()) {
                            hintLabel.setVisible(true); hintLabel.setManaged(true);
                            solverController.showGiveUpButton();
                        }
                    });

                    VBox codeCardWrapper = createQuestionContainer();
                    codeCardWrapper.setUserData(task.id());
                    codeCardWrapper.setFocusTraversable(true);
                    codeCardWrapper.getChildren().addAll(buildMetadataHeader(task), solverUI, hintLabel);
                    VBox.setMargin(codeCardWrapper, new Insets(0, 0, 30, 0));

                    practiceContainer.getChildren().add(codeCardWrapper);

                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private void handleCorrectAnswer(QuestionDTO q, boolean isGiveUp) {
        boolean alreadyFinishedBefore = isChapterFinished;
        currentLessonCount++;
        int dynamicTotalLessons = (uiQuestionOrder != null) ? uiQuestionOrder.size() : 0;

        if (chapter != null && currentLessonCount >= dynamicTotalLessons) {
            isChapterFinished = true;
            if (userProgress != null) userProgress.setCompleted(true);
        }

        try {
            if (currentUser != null) {
                if (userProgress != null) {
                    userProgress.setCompletedLessons(currentLessonCount);
                    if (isChapterFinished) userProgress.setCompleted(true);
                    progressService.saveProgressDto(userProgress, currentUser.getUsername());
                }

                if (!isGiveUp) {
                    int xpReward = (q != null) ? q.rewardXp() : 0;
                    int tokenReward = (q != null) ? q.rewardToken() : 0;

                    if (isChapterFinished && !alreadyFinishedBefore && chapter != null) {
                        xpReward += chapter.xpReward();
                        tokenReward += 50;
                        if (btnBack != null && btnBack.getScene() != null && btnBack.getScene().getRoot() instanceof javafx.scene.layout.Pane rootPane) {
                            com.codedu.ui.UIUtils.fireConfetti(rootPane);
                        }
                    }

                    if (xpReward > 0 || tokenReward > 0) {
                        User updatedUser = userService.awardXpAndTokensEntity(currentUser.getUsername(), xpReward, tokenReward);
                        if (updatedUser != null && onProgressUpdated != null) {
                            javafx.application.Platform.runLater(() -> onProgressUpdated.accept(updatedUser));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.runLater(() -> {
                updateHeaderProgress();

                long quizCount = uiQuestionOrder.stream()
                        .filter(quest -> quest.questionType() != QuestionType.CODE_IMPLEMENTATION)
                        .count();

                if (currentLessonCount == quizCount || currentLessonCount >= uiQuestionOrder.size()) {
                    List<QuestionDTO> codeQuestions = uiQuestionOrder.stream()
                            .filter(quest -> quest.questionType() == QuestionType.CODE_IMPLEMENTATION)
                            .collect(Collectors.toList());
                    buildPracticeSection(codeQuestions);
                } else {

                    System.out.println("Question handled locally, skipping full rebuild.");
                }
            });
        }
    }

    private void updateHeaderProgress() {
        int total = uiQuestionOrder.size();
        if (total == 0 && chapter != null) total = chapter.totalLessons();
        headerXP.setText(String.format("%d/%d Lessons", currentLessonCount, total));
    }

    private boolean isQuestionCompleted(QuestionDTO q) {
        if (isChapterFinished) return true;
        int globalIndex = -1;
        for (int i = 0; i < uiQuestionOrder.size(); i++) {
            if (uiQuestionOrder.get(i).id() == q.id()) {
                globalIndex = i;
                break;
            }
        }
        return (globalIndex != -1 && globalIndex < currentLessonCount);
    }

    public void setOnProgressUpdated(java.util.function.Consumer<User> onProgressUpdated) { this.onProgressUpdated = onProgressUpdated; }
    public void setOnBack(Runnable onBack) { this.onBack = onBack; btnBack.setOnAction(e -> { if (onBack != null) onBack.run(); }); }

    private boolean hasEnoughHearts() {
        if (currentUser != null && currentUser.getGameState() != null) {
            if (currentUser.getGameState().getHeartCount() <= 0) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Out of Hearts!"); alert.setHeaderText(null);
                alert.setContentText("You have 0 hearts remaining. Wait for them to refill or buy more in the store!");
                alert.showAndWait();
                return false;
            }
        }
        return true;
    }

    private void handleWrongAnswer() {
        if (currentUser != null) {
            userService.decrementHeart(currentUser.getUsername());
            userService.getUserWithProfileData(currentUser.getUsername()).ifPresent(freshUser -> {
                this.currentUser = freshUser;
                if (onProgressUpdated != null) javafx.application.Platform.runLater(() -> onProgressUpdated.accept(freshUser));
            });
        }
    }

    private String normalizeJavaCode(String input) {
        if (input == null) return "";
        return input.replaceAll("\\s*([=;+\\-*/(){}\\[\\]<>,])\\s*", "$1").replaceAll("\\s+", " ").trim();
    }

    private Label buildMetadataHeader(QuestionDTO q) {
        int xp = q.rewardXp();
        String difficulty = (q.questionDifficulty() != null) ? q.questionDifficulty().toString() : "NORMAL";

        String metaText = String.format("%s • %d XP", difficulty, xp);
        Label metaLabel = new Label(metaText);
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + LOGO_BLUE + "; -fx-font-weight: bold; -fx-opacity: 0.8;");
        return metaLabel;
    }

    private VBox createQuestionContainer() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 12; -fx-border-width: 1;");
        return container;
    }

    private int getDifficultyWeight(QuestionDTO q) {
        if (q.questionDifficulty() == null) return 0;
        String diff = q.questionDifficulty().toString().toUpperCase();

        if (diff.contains("EASY")) return 1;
        if (diff.contains("MEDIUM") || diff.contains("NORMAL")) return 2;
        if (diff.contains("HARD")) return 3;

        return 0;
    }
}
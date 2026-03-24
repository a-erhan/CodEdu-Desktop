package com.codedu.controllers;

import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.Chapter.Difficulty;
import com.codedu.models.learning.ChapterContent;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.FillInBlankQuestion;
import com.codedu.models.learning.MultipleChoiceQuestion;
import com.codedu.models.learning.Question;
import com.codedu.models.learning.QuestionType;
import com.codedu.models.learning.Reward;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for the Learning Path module.
 * Builds a vertical path of chapter cards inspired by Coddy's journey UI.
 */
@Controller
public class LearningPathController {

        @FXML
        private VBox pathContainer;
        @FXML
        private Label chapterCountLabel;

        // Detail panel
        @FXML
        private VBox detailPanel;
        @FXML
        private ImageView detailIconImage;
        @FXML
        private Label detailTitle;
        @FXML
        private Label detailDifficulty;
        @FXML
        private Label detailDescription;
        @FXML
        private Label detailLessons;
        @FXML
        private Label detailXP;
        @FXML
        private Label detailAction;

        // UI now tracks the DTO, not just the raw Chapter entity
        private final List<ChapterProgressDTO> chapters = new ArrayList<>();
        private HBox selectedCard = null;
        private ChapterProgressDTO selectedChapter = null;

        /** Callback set by MainShellController to load chapter content view. */
        private Consumer<Chapter> onStartChapter;

        public void setOnStartChapter(Consumer<Chapter> callback) {
                this.onStartChapter = callback;
        }

        @FXML
        public void initialize() {
                loadPlaceholderChapters();
                chapterCountLabel.setText(chapters.size() + " Chapters");
                buildPath();
        }

        // ══════════════════════════════════════════════════════════════════
        // DTO (Data Transfer Object) for UI State
        // ══════════════════════════════════════════════════════════════════

        public static class ChapterProgressDTO {
                private final Chapter chapter;
                private final int completedLessons;
                private final boolean isLocked;
                private final boolean isCompleted;

                public ChapterProgressDTO(Chapter chapter, int completedLessons, boolean isLocked, boolean isCompleted) {
                        this.chapter = chapter;
                        this.completedLessons = completedLessons;
                        this.isLocked = isLocked;
                        this.isCompleted = isCompleted;
                }

                public Chapter getChapter() { return chapter; }
                public int getCompletedLessons() { return completedLessons; }
                public boolean isLocked() { return isLocked; }
                public boolean isCompleted() { return isCompleted; }
                public double getProgress() {
                        if (chapter.getTotalLessons() == 0) return 0.0;
                        return (double) completedLessons / chapter.getTotalLessons();
                }

                // Delegate getters to make UI code cleaner
                public String getTitle() { return chapter.getTitle(); }
                public String getDescription() { return chapter.getDescription(); }
                public String getIconEmoji() { return chapter.getIconEmoji(); }
                public String getIconImage() { return chapter.getIconImage(); }
                public int getTotalLessons() { return chapter.getTotalLessons(); }
                public int getXpReward() { return chapter.getXpReward(); }
                public Difficulty getDifficulty() { return chapter.getDifficulty(); }
        }

        // ══════════════════════════════════════════════════════════════════
        // HELPER: create Question objects
        // ══════════════════════════════════════════════════════════════════

        private static Question mc(String title, String content, String solution, String hint) {
                MultipleChoiceQuestion q = new MultipleChoiceQuestion();
                q.setQuestionType(QuestionType.MULTIPLE_CHOICES);
                q.setContent(content);
                q.setTitle(title);
                q.setSolution(solution);
                q.setHint(hint);
                q.setReward(new Reward(5, 10));
                return q;
        }

        private static Question fb(String title, String content, String solution, String hint) {
                FillInBlankQuestion q = new FillInBlankQuestion();
                q.setQuestionType(QuestionType.FILL_IN_THE_BLANKS);
                q.setContent(content);
                q.setTitle(title);
                q.setSolution(solution);
                q.setHint(hint);
                q.setReward(new Reward(5, 10));
                return q;
        }

        private static Question code(String title, String content, String solution, String hint) {
                CodeImplementationQuestion q = new CodeImplementationQuestion();
                q.setQuestionType(QuestionType.CODE_IMPLEMENTATION);
                q.setContent(content);
                q.setTitle(title);
                q.setSolution(solution);
                q.setHint(hint);
                q.setReward(new Reward(10, 20));
                return q;
        }

        // ══════════════════════════════════════════════════════════════════
        // CHAPTER DATA + MOCK CONTENT
        // ══════════════════════════════════════════════════════════════════

        private void loadPlaceholderChapters() {
                // ── 1. Hello World ────────────────────────────────
                Chapter ch1 = new Chapter("Hello, World!", "Your very first program — learn to print output and understand program structure.", "👋", "/com/codedu/images/ch_hello_world.png", Difficulty.BEGINNER, 5, 50);
                ch1.setOrderIndex(1);
                ch1.setContent(new ChapterContent("# Hello, World!\n\nEvery programmer's journey begins with a single line of code. The classic \"Hello, World!\" program teaches you how to display output.\n\n## Your First Program\n\nIn Java, every program starts inside a class with a `main` method:\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n```\n\n## Key Concepts\n\n- `System.out.println()` prints text to the console\n- Every Java program needs a `main` method\n- Strings are enclosed in double quotes\n- Statements end with a semicolon `;`", List.of(mc("Print Method", "What method is used to print output in Java?\nA) System.out.println()\nB) print()\nC) echo()\nD) console.log()", "A", "It starts with System.out"))));
                chapters.add(new ChapterProgressDTO(ch1, 5, false, true));

                // ── 2. Variables & Data Types ─────────────────────
                Chapter ch2 = new Chapter("Variables & Data Types", "Store and manipulate data using variables, strings, integers, and booleans.", "📦", "/com/codedu/images/ch_variables.png", Difficulty.BEGINNER, 8, 80);
                ch2.setOrderIndex(2);
                ch2.setContent(new ChapterContent("# Variables & Data Types\n\nVariables are containers for storing data values. Java is a statically-typed language, meaning you must declare the type of each variable.\n\n## Primitive Types\n\n- `int` — whole numbers\n- `double` — decimal numbers\n- `boolean` — true or false\n- `char` — single character\n\n## Declaring Variables\n\n```java\nint age = 25;\ndouble price = 9.99;\nboolean isActive = true;\nString name = \"Alice\";\n```", List.of(fb("int Fill", "___ score = 100;", "int", "This type stores whole numbers"))));
                chapters.add(new ChapterProgressDTO(ch2, 8, false, true));

                // ── 3. Operators & Expressions ────────────────────
                Chapter ch3 = new Chapter("Operators & Expressions", "Master arithmetic, comparison, and logical operators to build expressions.", "➖", "/com/codedu/images/ch_operators.png", Difficulty.BEGINNER, 6, 60);
                ch3.setOrderIndex(3);
                ch3.setContent(new ChapterContent("# Operators & Expressions\n\nOperators perform operations on variables and values.\n\n## Arithmetic: + - * / %\n## Comparison: == != < > <= >=\n## Logical: && || !", List.of(mc("Modulus", "What does the % operator return?\nA) Quotient\nB) Product\nC) Remainder\nD) Percentage", "C", "Think about division remainder"))));
                chapters.add(new ChapterProgressDTO(ch3, 6, false, true));

                // ── 4. Control Flow: If/Else ──────────────────────
                Chapter ch4 = new Chapter("Control Flow: If/Else", "Make decisions in your code using conditional statements and branching logic.", "🔀", "/com/codedu/images/ch_control_flow.png", Difficulty.BEGINNER, 7, 70);
                ch4.setOrderIndex(4);
                ch4.setContent(new ChapterContent("# Control Flow: If/Else\n\nConditional statements let your program make decisions.", List.of(code("Positive/Negative", "Write if/else that prints Positive, Negative, or Zero based on variable num.", "Negative", "Use if/else if/else chain"))));
                chapters.add(new ChapterProgressDTO(ch4, 4, false, false)); // 4/7 completed, not locked

                // ── 5. Loops: For & While ─────────────────────────
                Chapter ch5 = new Chapter("Loops: For & While", "Repeat actions efficiently with for-loops, while-loops, and iteration patterns.", "🔁", "/com/codedu/images/ch_loops.png", Difficulty.INTERMEDIATE, 9, 100);
                ch5.setOrderIndex(5);
                ch5.setContent(new ChapterContent("# Loops: For & While\n\nLoops let you execute a block of code multiple times.", List.of(fb("While Fill", "___ (condition) { // repeat }", "while", "Loop keyword that runs while condition is true"))));
                chapters.add(new ChapterProgressDTO(ch5, 2, false, false)); // 2/9 completed

                // ── 6. Functions & Methods ────────────────────────
                Chapter ch6 = new Chapter("Functions & Methods", "Write reusable blocks of code, understand parameters, return values, and scope.", "⚙️", "/com/codedu/images/ch_functions.png", Difficulty.INTERMEDIATE, 10, 120);
                ch6.setOrderIndex(6);
                chapters.add(new ChapterProgressDTO(ch6, 0, false, false));

                // ── 7. Arrays & Collections ───────────────────────
                Chapter ch7 = new Chapter("Arrays & Collections", "Organize data with arrays, lists, and maps. Learn indexing and iteration.", "📚", "/com/codedu/images/ch_arrays.png", Difficulty.INTERMEDIATE, 8, 100);
                ch7.setOrderIndex(7);
                chapters.add(new ChapterProgressDTO(ch7, 0, false, false));

                // ── 8. OOP (Advanced) ─────────────────────────────
                Chapter ch8 = new Chapter("Object-Oriented Programming", "Design classes, objects, inheritance, and polymorphism like a pro.", "🏗️", "/com/codedu/images/ch_oop.png", Difficulty.ADVANCED, 12, 200);
                ch8.setOrderIndex(8);
                chapters.add(new ChapterProgressDTO(ch8, 0, false, false));

                // ── 9. Linear Data Structures (Advanced) ──────────
                Chapter ch9 = new Chapter("Linear Data Structures", "Master arrays, linked lists, stacks, and queues — the building blocks of algorithms.", "🔗", "/com/codedu/images/ch_linear_ds.png", Difficulty.ADVANCED, 10, 180);
                ch9.setOrderIndex(9);
                chapters.add(new ChapterProgressDTO(ch9, 0, false, false));

                // ── 10. Non-Linear Data Structures (Advanced) ─────
                Chapter ch10 = new Chapter("Non-Linear Data Structures", "Explore trees, graphs, and hash maps — essential for advanced algorithms.", "🌳", "/com/codedu/images/ch_nonlinear_ds.png", Difficulty.ADVANCED, 10, 200);
                ch10.setOrderIndex(10);
                chapters.add(new ChapterProgressDTO(ch10, 0, false, false));
        }

        // ══════════════════════════════════════════════════════════════════
        // PATH BUILDING
        // ══════════════════════════════════════════════════════════════════

        private void buildPath() {
                pathContainer.getChildren().clear();

                for (int i = 0; i < chapters.size(); i++) {
                        ChapterProgressDTO chapter = chapters.get(i);

                        if (i > 0) {
                                pathContainer.getChildren().add(buildConnector(chapter));
                        }

                        HBox card = buildChapterCard(chapter, i);

                        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
                        fade.setFromValue(0);
                        fade.setToValue(1);
                        fade.setDelay(Duration.millis(i * 80));

                        TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
                        slide.setFromY(20);
                        slide.setToY(0);
                        slide.setDelay(Duration.millis(i * 80));

                        card.setOpacity(0);
                        pathContainer.getChildren().add(card);

                        fade.play();
                        slide.play();
                }
        }

        private VBox buildConnector(ChapterProgressDTO nextChapter) {
                VBox connectorBox = new VBox();
                connectorBox.setAlignment(Pos.CENTER);
                connectorBox.setPrefHeight(36);
                connectorBox.setMinHeight(36);
                connectorBox.setMaxHeight(36);

                Region line = new Region();
                line.getStyleClass().add("chapter-connector");
                if (nextChapter.isLocked()) {
                        line.getStyleClass().add("chapter-connector-locked");
                } else if (nextChapter.isCompleted()) {
                        line.getStyleClass().add("chapter-connector-completed");
                }
                line.setPrefWidth(3);
                line.setMaxWidth(3);
                line.setPrefHeight(36);
                VBox.setVgrow(line, Priority.ALWAYS);

                connectorBox.getChildren().add(line);
                return connectorBox;
        }

        private HBox buildChapterCard(ChapterProgressDTO chapter, int index) {
                HBox card = new HBox(16);
                card.getStyleClass().add("chapter-card");
                card.setPadding(new Insets(16, 20, 16, 16));
                card.setAlignment(Pos.CENTER_LEFT);

                if (chapter.isCompleted()) {
                        card.getStyleClass().add("chapter-card-completed");
                } else if (chapter.isLocked()) {
                        card.getStyleClass().add("chapter-card-locked");
                }

                StackPane iconCircle = new StackPane();
                iconCircle.getStyleClass().add("chapter-icon");
                if (chapter.isCompleted()) {
                        iconCircle.getStyleClass().add("chapter-icon-completed");
                } else if (chapter.isLocked()) {
                        iconCircle.getStyleClass().add("chapter-icon-locked");
                }
                iconCircle.setMinSize(52, 52);
                iconCircle.setMaxSize(52, 52);

                if (chapter.isLocked()) {
                        Label lockLabel = new Label("Locked");
                        iconCircle.getChildren().add(lockLabel);
                } else if (chapter.getIconImage() != null) {
                        try {
                                Image img = new Image(getClass().getResourceAsStream(chapter.getIconImage()));
                                ImageView iv = new ImageView(img);
                                iv.setFitWidth(40);
                                iv.setFitHeight(40);
                                iv.setPreserveRatio(true);
                                iv.setSmooth(true);
                                iconCircle.getChildren().add(iv);
                        } catch (Exception e) {
                                Label iconLabel = new Label(chapter.getIconEmoji());
                                iconCircle.getChildren().add(iconLabel);
                        }
                } else {
                        Label iconLabel = new Label(chapter.getIconEmoji());
                        iconCircle.getChildren().add(iconLabel);
                }

                VBox infoBox = new VBox(4);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                HBox titleRow = new HBox(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);

                Label titleLabel = new Label(chapter.getTitle());
                titleLabel.getStyleClass().add("chapter-title");

                Label diffTag = new Label(difficultyText(chapter.getDifficulty()));
                diffTag.getStyleClass().addAll("difficulty-tag", difficultyClass(chapter.getDifficulty()));

                titleRow.getChildren().addAll(titleLabel, diffTag);

                Label descLabel = new Label(chapter.getDescription());
                descLabel.getStyleClass().add("chapter-desc");
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(420);

                HBox progressRow = new HBox(10);
                progressRow.setAlignment(Pos.CENTER_LEFT);

                ProgressBar progressBar = new ProgressBar(chapter.getProgress());
                progressBar.getStyleClass().add("chapter-progress-bar");
                progressBar.setPrefWidth(140);
                progressBar.setPrefHeight(8);

                Label progressLabel = new Label(chapter.getCompletedLessons() + "/" + chapter.getTotalLessons() + " lessons");
                progressLabel.getStyleClass().add("chapter-progress-text");

                progressRow.getChildren().addAll(progressBar, progressLabel);

                infoBox.getChildren().addAll(titleRow, descLabel, progressRow);

                VBox xpBox = new VBox(4);
                xpBox.setAlignment(Pos.CENTER);
                xpBox.setMinWidth(70);

                Label xpLabel = new Label("+" + chapter.getXpReward());
                xpLabel.getStyleClass().add("chapter-xp-value");

                Label xpText = new Label("XP");
                xpText.getStyleClass().add("chapter-xp-label");

                if (chapter.isCompleted()) {
                        Label checkLabel = new Label("✓");
                        checkLabel.getStyleClass().add("chapter-check");
                        xpBox.getChildren().addAll(checkLabel, xpText);
                } else {
                        xpBox.getChildren().addAll(xpLabel, xpText);
                }

                card.getChildren().addAll(iconCircle, infoBox, xpBox);

                if (!chapter.isLocked()) {
                        card.setOnMouseEntered(e -> {
                                ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
                                st.setToX(1.02);
                                st.setToY(1.02);
                                st.play();
                        });
                        card.setOnMouseExited(e -> {
                                ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
                                st.setToX(1.0);
                                st.setToY(1.0);
                                st.play();
                        });
                        card.setOnMouseClicked(e -> showDetail(chapter, card));
                }

                return card;
        }

        // ══════════════════════════════════════════════════════════════════
        // DETAIL PANEL
        // ══════════════════════════════════════════════════════════════════

        private void showDetail(ChapterProgressDTO chapter, HBox card) {
                if (selectedCard != null) {
                        selectedCard.getStyleClass().remove("chapter-card-selected");
                }
                selectedCard = card;
                selectedChapter = chapter;
                card.getStyleClass().add("chapter-card-selected");

                if (chapter.getIconImage() != null) {
                        try {
                                Image img = new Image(getClass().getResourceAsStream(chapter.getIconImage()));
                                detailIconImage.setImage(img);
                        } catch (Exception e) {
                                // Fallback if image path is broken
                        }
                }
                detailTitle.setText(chapter.getTitle());
                detailDifficulty.setText(difficultyText(chapter.getDifficulty()));
                detailDifficulty.getStyleClass().removeAll("diff-beginner", "diff-intermediate", "diff-advanced");
                detailDifficulty.getStyleClass().add(difficultyClass(chapter.getDifficulty()));
                detailDescription.setText(chapter.getDescription());
                detailLessons.setText(chapter.getCompletedLessons() + " / " + chapter.getTotalLessons() + " lessons completed");
                detailXP.setText(chapter.getXpReward() + " XP reward");

                if (chapter.isCompleted()) {
                        detailAction.setText("Completed");
                        detailAction.getStyleClass().removeAll("lp-detail-action");
                        detailAction.getStyleClass().add("lp-detail-action-done");
                        detailAction.setOnMouseClicked(e -> {
                                if (onStartChapter != null)
                                        onStartChapter.accept(selectedChapter.getChapter());
                        });
                } else {
                        int pct = (int) (chapter.getProgress() * 100);
                        detailAction.setText(pct > 0 ? "Continue (" + pct + "%)" : "Start chapter");
                        detailAction.getStyleClass().removeAll("lp-detail-action-done");
                        if (!detailAction.getStyleClass().contains("lp-detail-action")) {
                                detailAction.getStyleClass().add("lp-detail-action");
                        }
                        detailAction.setOnMouseClicked(e -> {
                                if (onStartChapter != null)
                                        onStartChapter.accept(selectedChapter.getChapter()); // Pass raw Chapter back to main shell
                        });
                }

                detailAction.setOnMouseEntered(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(100), detailAction);
                        st.setToX(1.05);
                        st.setToY(1.05);
                        st.play();
                });
                detailAction.setOnMouseExited(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(100), detailAction);
                        st.setToX(1.0);
                        st.setToY(1.0);
                        st.play();
                });

                if (!detailPanel.isVisible()) {
                        detailPanel.setVisible(true);
                        detailPanel.setManaged(true);
                        detailPanel.setTranslateX(280);
                        TranslateTransition tt = new TranslateTransition(Duration.millis(300), detailPanel);
                        tt.setToX(0);
                        tt.play();
                }
        }

        private String difficultyText(Difficulty d) {
                return switch (d) {
                        case BEGINNER -> "Beginner";
                        case INTERMEDIATE -> "Intermediate";
                        case ADVANCED -> "Advanced";
                };
        }

        private String difficultyClass(Difficulty d) {
                return switch (d) {
                        case BEGINNER -> "diff-beginner";
                        case INTERMEDIATE -> "diff-intermediate";
                        case ADVANCED -> "diff-advanced";
                };
        }
}
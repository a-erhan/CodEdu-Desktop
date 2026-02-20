<<<<<<< HEAD
package com.codedu.controllers;

import com.codedu.models.Chapter;
import com.codedu.models.Chapter.Difficulty;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Learning Path module.
 * Builds a vertical path of chapter cards inspired by Coddy's journey UI.
 */
public class LearningPathController {

    @FXML
    private VBox pathContainer;
    @FXML
    private Label chapterCountLabel;

    // Detail panel
    @FXML
    private VBox detailPanel;
    @FXML
    private Label detailIcon;
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

    private final List<Chapter> chapters = new ArrayList<>();
    private HBox selectedCard = null;

    @FXML
    public void initialize() {
        loadPlaceholderChapters();
        chapterCountLabel.setText(chapters.size() + " Chapters");
        buildPath();
    }

    private void loadPlaceholderChapters() {
        chapters.add(new Chapter(
                "Hello, World!", "Your very first program — learn to print output and understand program structure.",
                "\uD83D\uDC4B", Difficulty.BEGINNER, 5, 5, 50, false));
        chapters.add(new Chapter(
                "Variables & Data Types", "Store and manipulate data using variables, strings, integers, and booleans.",
                "\uD83D\uDCE6", Difficulty.BEGINNER, 8, 8, 80, false));
        chapters.add(new Chapter(
                "Operators & Expressions", "Master arithmetic, comparison, and logical operators to build expressions.",
                "\u2796", Difficulty.BEGINNER, 6, 6, 60, false));
        chapters.add(new Chapter(
                "Control Flow: If/Else",
                "Make decisions in your code using conditional statements and branching logic.",
                "\uD83D\uDD00", Difficulty.BEGINNER, 7, 4, 70, false));
        chapters.add(new Chapter(
                "Loops: For & While", "Repeat actions efficiently with for-loops, while-loops, and iteration patterns.",
                "\uD83D\uDD01", Difficulty.INTERMEDIATE, 9, 2, 100, false));
        chapters.add(new Chapter(
                "Functions & Methods",
                "Write reusable blocks of code, understand parameters, return values, and scope.",
                "\u2699\uFE0F", Difficulty.INTERMEDIATE, 10, 0, 120, false));
        chapters.add(new Chapter(
                "Arrays & Collections", "Organize data with arrays, lists, and maps. Learn indexing and iteration.",
                "\uD83D\uDCDA", Difficulty.INTERMEDIATE, 8, 0, 100, true));
        chapters.add(new Chapter(
                "Object-Oriented Programming", "Design classes, objects, inheritance, and polymorphism like a pro.",
                "\uD83C\uDFD7\uFE0F", Difficulty.ADVANCED, 12, 0, 200, true));
        chapters.add(new Chapter(
                "Error Handling", "Catch and handle exceptions gracefully. Write robust, crash-resistant code.",
                "\uD83D\uDEE1\uFE0F", Difficulty.ADVANCED, 6, 0, 80, true));
        chapters.add(new Chapter(
                "Final Project: Build a Game",
                "Apply everything you've learned to build a complete text-based adventure game!",
                "\uD83C\uDFAE", Difficulty.ADVANCED, 15, 0, 500, true));
    }

    private void buildPath() {
        pathContainer.getChildren().clear();

        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);

            // Add connector line before each card (except first)
            if (i > 0) {
                pathContainer.getChildren().add(buildConnector(chapter));
            }

            HBox card = buildChapterCard(chapter, i);

            // Stagger animation
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

    private VBox buildConnector(Chapter nextChapter) {
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

    private HBox buildChapterCard(Chapter chapter, int index) {
        // ── Main card wrapper ──
        HBox card = new HBox(16);
        card.getStyleClass().add("chapter-card");
        card.setPadding(new Insets(16, 20, 16, 16));
        card.setAlignment(Pos.CENTER_LEFT);

        if (chapter.isCompleted()) {
            card.getStyleClass().add("chapter-card-completed");
        } else if (chapter.isLocked()) {
            card.getStyleClass().add("chapter-card-locked");
        }

        // ── Left: Icon circle ──
        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("chapter-icon");
        if (chapter.isCompleted()) {
            iconCircle.getStyleClass().add("chapter-icon-completed");
        } else if (chapter.isLocked()) {
            iconCircle.getStyleClass().add("chapter-icon-locked");
        }
        iconCircle.setMinSize(52, 52);
        iconCircle.setMaxSize(52, 52);

        Label iconLabel = new Label(chapter.isLocked() ? "\uD83D\uDD12" : chapter.getIconEmoji());
        iconLabel.setStyle("-fx-font-size: 24px;");
        iconCircle.getChildren().add(iconLabel);

        // ── Center: Info area ──
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(chapter.getTitle());
        titleLabel.getStyleClass().add("chapter-title");

        Label diffTag = new Label(difficultyText(chapter.getDifficulty()));
        diffTag.getStyleClass().addAll("difficulty-tag", difficultyClass(chapter.getDifficulty()));

        titleRow.getChildren().addAll(titleLabel, diffTag);

        // Description
        Label descLabel = new Label(chapter.getDescription());
        descLabel.getStyleClass().add("chapter-desc");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(420);

        // Progress row
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

        // ── Right: XP badge ──
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

        // ── Chapter number indicator ──
        Label numLabel = new Label(String.valueOf(index + 1));
        numLabel.getStyleClass().add("chapter-num");

        // ── Hover animations (only for unlocked) ──
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

    private void showDetail(Chapter chapter, HBox card) {
        // Highlight selected card
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("chapter-card-selected");
        }
        selectedCard = card;
        card.getStyleClass().add("chapter-card-selected");

        // Populate detail panel
        detailIcon.setText(chapter.getIconEmoji());
        detailIcon.setStyle("-fx-font-size: 48px;");
        detailTitle.setText(chapter.getTitle());
        detailDifficulty.setText(difficultyText(chapter.getDifficulty()));
        detailDifficulty.getStyleClass().removeAll("diff-beginner", "diff-intermediate", "diff-advanced");
        detailDifficulty.getStyleClass().add(difficultyClass(chapter.getDifficulty()));
        detailDescription.setText(chapter.getDescription());
        detailLessons.setText("\uD83D\uDCD6  " + chapter.getCompletedLessons() + " / "
                + chapter.getTotalLessons() + " lessons completed");
        detailXP.setText("\u2B50  " + chapter.getXpReward() + " XP reward");

        if (chapter.isCompleted()) {
            detailAction.setText("✓  Completed");
            detailAction.getStyleClass().removeAll("lp-detail-action");
            detailAction.getStyleClass().add("lp-detail-action-done");
        } else {
            int pct = (int) (chapter.getProgress() * 100);
            detailAction.setText(pct > 0 ? "▶  Continue (" + pct + "%)" : "▶  Start Chapter");
            detailAction.getStyleClass().removeAll("lp-detail-action-done");
            if (!detailAction.getStyleClass().contains("lp-detail-action")) {
                detailAction.getStyleClass().add("lp-detail-action");
            }
        }

        // Slide-in animation
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
=======
package com.codedu.controllers;

import com.codedu.models.Chapter;
import com.codedu.models.Chapter.Difficulty;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Learning Path module.
 * Builds a vertical path of chapter cards inspired by Coddy's journey UI.
 */
public class LearningPathController {

    @FXML
    private VBox pathContainer;
    @FXML
    private Label chapterCountLabel;

    // Detail panel
    @FXML
    private VBox detailPanel;
    @FXML
    private Label detailIcon;
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

    private final List<Chapter> chapters = new ArrayList<>();
    private HBox selectedCard = null;

    @FXML
    public void initialize() {
        loadPlaceholderChapters();
        chapterCountLabel.setText(chapters.size() + " Chapters");
        buildPath();
    }

    private void loadPlaceholderChapters() {
        chapters.add(new Chapter(
                "Hello, World!", "Your very first program — learn to print output and understand program structure.",
                "\uD83D\uDC4B", Difficulty.BEGINNER, 5, 5, 50, false));
        chapters.add(new Chapter(
                "Variables & Data Types", "Store and manipulate data using variables, strings, integers, and booleans.",
                "\uD83D\uDCE6", Difficulty.BEGINNER, 8, 8, 80, false));
        chapters.add(new Chapter(
                "Operators & Expressions", "Master arithmetic, comparison, and logical operators to build expressions.",
                "\u2796", Difficulty.BEGINNER, 6, 6, 60, false));
        chapters.add(new Chapter(
                "Control Flow: If/Else",
                "Make decisions in your code using conditional statements and branching logic.",
                "\uD83D\uDD00", Difficulty.BEGINNER, 7, 4, 70, false));
        chapters.add(new Chapter(
                "Loops: For & While", "Repeat actions efficiently with for-loops, while-loops, and iteration patterns.",
                "\uD83D\uDD01", Difficulty.INTERMEDIATE, 9, 2, 100, false));
        chapters.add(new Chapter(
                "Functions & Methods",
                "Write reusable blocks of code, understand parameters, return values, and scope.",
                "\u2699\uFE0F", Difficulty.INTERMEDIATE, 10, 0, 120, false));
        chapters.add(new Chapter(
                "Arrays & Collections", "Organize data with arrays, lists, and maps. Learn indexing and iteration.",
                "\uD83D\uDCDA", Difficulty.INTERMEDIATE, 8, 0, 100, true));
        chapters.add(new Chapter(
                "Object-Oriented Programming", "Design classes, objects, inheritance, and polymorphism like a pro.",
                "\uD83C\uDFD7\uFE0F", Difficulty.ADVANCED, 12, 0, 200, true));
        chapters.add(new Chapter(
                "Error Handling", "Catch and handle exceptions gracefully. Write robust, crash-resistant code.",
                "\uD83D\uDEE1\uFE0F", Difficulty.ADVANCED, 6, 0, 80, true));
        chapters.add(new Chapter(
                "Final Project: Build a Game",
                "Apply everything you've learned to build a complete text-based adventure game!",
                "\uD83C\uDFAE", Difficulty.ADVANCED, 15, 0, 500, true));
    }

    private void buildPath() {
        pathContainer.getChildren().clear();

        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);

            // Add connector line before each card (except first)
            if (i > 0) {
                pathContainer.getChildren().add(buildConnector(chapter));
            }

            HBox card = buildChapterCard(chapter, i);

            // Stagger animation
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

    private VBox buildConnector(Chapter nextChapter) {
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

    private HBox buildChapterCard(Chapter chapter, int index) {
        // ── Main card wrapper ──
        HBox card = new HBox(16);
        card.getStyleClass().add("chapter-card");
        card.setPadding(new Insets(16, 20, 16, 16));
        card.setAlignment(Pos.CENTER_LEFT);

        if (chapter.isCompleted()) {
            card.getStyleClass().add("chapter-card-completed");
        } else if (chapter.isLocked()) {
            card.getStyleClass().add("chapter-card-locked");
        }

        // ── Left: Icon circle ──
        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("chapter-icon");
        if (chapter.isCompleted()) {
            iconCircle.getStyleClass().add("chapter-icon-completed");
        } else if (chapter.isLocked()) {
            iconCircle.getStyleClass().add("chapter-icon-locked");
        }
        iconCircle.setMinSize(52, 52);
        iconCircle.setMaxSize(52, 52);

        Label iconLabel = new Label(chapter.isLocked() ? "\uD83D\uDD12" : chapter.getIconEmoji());
        iconLabel.setStyle("-fx-font-size: 24px;");
        iconCircle.getChildren().add(iconLabel);

        // ── Center: Info area ──
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(chapter.getTitle());
        titleLabel.getStyleClass().add("chapter-title");

        Label diffTag = new Label(difficultyText(chapter.getDifficulty()));
        diffTag.getStyleClass().addAll("difficulty-tag", difficultyClass(chapter.getDifficulty()));

        titleRow.getChildren().addAll(titleLabel, diffTag);

        // Description
        Label descLabel = new Label(chapter.getDescription());
        descLabel.getStyleClass().add("chapter-desc");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(420);

        // Progress row
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

        // ── Right: XP badge ──
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

        // ── Chapter number indicator ──
        Label numLabel = new Label(String.valueOf(index + 1));
        numLabel.getStyleClass().add("chapter-num");

        // ── Hover animations (only for unlocked) ──
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

    private void showDetail(Chapter chapter, HBox card) {
        // Highlight selected card
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("chapter-card-selected");
        }
        selectedCard = card;
        card.getStyleClass().add("chapter-card-selected");

        // Populate detail panel
        detailIcon.setText(chapter.getIconEmoji());
        detailIcon.setStyle("-fx-font-size: 48px;");
        detailTitle.setText(chapter.getTitle());
        detailDifficulty.setText(difficultyText(chapter.getDifficulty()));
        detailDifficulty.getStyleClass().removeAll("diff-beginner", "diff-intermediate", "diff-advanced");
        detailDifficulty.getStyleClass().add(difficultyClass(chapter.getDifficulty()));
        detailDescription.setText(chapter.getDescription());
        detailLessons.setText("\uD83D\uDCD6  " + chapter.getCompletedLessons() + " / "
                + chapter.getTotalLessons() + " lessons completed");
        detailXP.setText("\u2B50  " + chapter.getXpReward() + " XP reward");

        if (chapter.isCompleted()) {
            detailAction.setText("✓  Completed");
            detailAction.getStyleClass().removeAll("lp-detail-action");
            detailAction.getStyleClass().add("lp-detail-action-done");
        } else {
            int pct = (int) (chapter.getProgress() * 100);
            detailAction.setText(pct > 0 ? "▶  Continue (" + pct + "%)" : "▶  Start Chapter");
            detailAction.getStyleClass().removeAll("lp-detail-action-done");
            if (!detailAction.getStyleClass().contains("lp-detail-action")) {
                detailAction.getStyleClass().add("lp-detail-action");
            }
        }

        // Slide-in animation
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
>>>>>>> 68084a056cecabbca83ad1f2bea89e41b0d5769a

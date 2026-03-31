package com.codedu.controllers;

import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.Chapter.Difficulty;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.LearningPathService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for the Learning Path module.
 * Fetches real chapter data and progress from the LearningPathService.
 */
@Controller
public class LearningPathController {

        @Autowired
        private LearningPathService learningPathService;

        private User currentUser;

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

        private List<ChapterProgressDTO> chapters;
        private HBox selectedCard = null;
        private ChapterProgressDTO selectedChapter = null;

        /** Callback set by MainShellController to load chapter content view. */
        private Consumer<Chapter> onStartChapter;

        public void setOnStartChapter(Consumer<Chapter> callback) {
                this.onStartChapter = callback;
        }

        public void setCurrentUser(User user) {
                this.currentUser = user;
        }

        /**
         * Loads chapters for {@link #currentUser}. Called from {@link MainShellController} after the shell user is set.
         */
        public void refreshPath() {
                if (pathContainer != null) {
                        pathContainer.getChildren().clear();
                }
                if (currentUser == null || currentUser.getId() <= 0) {
                        this.chapters = Collections.emptyList();
                        if (chapterCountLabel != null) {
                                chapterCountLabel.setText("—");
                        }
                        return;
                }
                this.chapters = learningPathService.getLearningPathForUser(currentUser);
                if (chapterCountLabel != null) {
                        chapterCountLabel.setText(chapters.size() + " Chapters");
                }
                buildPath();
        }

        @FXML
        public void initialize() {
                if (chapterCountLabel != null) {
                        chapterCountLabel.setText("—");
                }
        }

        // ══════════════════════════════════════════════════════════════════
        // PATH BUILDING (Uses DTO data)
        // ══════════════════════════════════════════════════════════════════

        private void buildPath() {
                pathContainer.getChildren().clear();

                for (int i = 0; i < chapters.size(); i++) {
                        ChapterProgressDTO chapterDto = chapters.get(i);

                        if (i > 0) {
                                pathContainer.getChildren().add(buildConnector(chapterDto));
                        }

                        HBox card = buildChapterCard(chapterDto, i);

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

        private HBox buildChapterCard(ChapterProgressDTO chapterDto, int index) {
                HBox card = new HBox(16);
                card.getStyleClass().add("chapter-card");
                card.setPadding(new Insets(16, 20, 16, 16));
                card.setAlignment(Pos.CENTER_LEFT);

                if (chapterDto.isCompleted()) {
                        card.getStyleClass().add("chapter-card-completed");
                } else if (chapterDto.isLocked()) {
                        card.getStyleClass().add("chapter-card-locked");
                }

                StackPane iconCircle = new StackPane();
                iconCircle.getStyleClass().add("chapter-icon");

                if (chapterDto.isCompleted()) {
                        iconCircle.getStyleClass().add("chapter-icon-completed");
                } else if (chapterDto.isLocked()) {
                        iconCircle.getStyleClass().add("chapter-icon-locked");
                }

                iconCircle.setMinSize(52, 52);
                iconCircle.setMaxSize(52, 52);

                if (chapterDto.isLocked()) {
                        Label lockLabel = new Label("Locked");
                        iconCircle.getChildren().add(lockLabel);
                } else if (chapterDto.getChapter().getIconImage() != null) {
                        try {
                                Image img = new Image(getClass().getResourceAsStream(chapterDto.getChapter().getIconImage()));
                                ImageView iv = new ImageView(img);
                                iv.setFitWidth(40);
                                iv.setFitHeight(40);
                                iv.setPreserveRatio(true);
                                iv.setSmooth(true);
                                iconCircle.getChildren().add(iv);
                        } catch (Exception e) {
                                Label iconLabel = new Label(chapterDto.getChapter().getIconEmoji());
                                iconCircle.getChildren().add(iconLabel);
                        }
                } else {
                        Label iconLabel = new Label(chapterDto.getChapter().getIconEmoji());
                        iconCircle.getChildren().add(iconLabel);
                }

                VBox infoBox = new VBox(4);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                HBox titleRow = new HBox(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);

                Label titleLabel = new Label(chapterDto.getChapter().getTitle());
                titleLabel.getStyleClass().add("chapter-title");

                Label diffTag = new Label(difficultyText(chapterDto.getChapter().getDifficulty()));
                diffTag.getStyleClass().addAll("difficulty-tag", difficultyClass(chapterDto.getChapter().getDifficulty()));

                titleRow.getChildren().addAll(titleLabel, diffTag);

                Label descLabel = new Label(chapterDto.getChapter().getDescription());
                descLabel.getStyleClass().add("chapter-desc");
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(420);

                HBox progressRow = new HBox(10);
                progressRow.setAlignment(Pos.CENTER_LEFT);

                ProgressBar progressBar = new ProgressBar(chapterDto.getProgress());
                progressBar.getStyleClass().add("chapter-progress-bar");
                progressBar.setPrefWidth(140);
                progressBar.setPrefHeight(8);

                Label progressLabel = new Label(chapterDto.getCompletedLessons() + "/" + chapterDto.getChapter().getTotalLessons() + " lessons");
                progressLabel.getStyleClass().add("chapter-progress-text");

                progressRow.getChildren().addAll(progressBar, progressLabel);

                infoBox.getChildren().addAll(titleRow, descLabel, progressRow);

                VBox xpBox = new VBox(4);
                xpBox.setAlignment(Pos.CENTER);
                xpBox.setMinWidth(70);

                Label xpLabel = new Label("+" + chapterDto.getChapter().getXpReward());
                xpLabel.getStyleClass().add("chapter-xp-value");

                Label xpText = new Label("XP");
                xpText.getStyleClass().add("chapter-xp-label");

                if (chapterDto.isCompleted()) {
                        Label checkLabel = new Label("✓");
                        checkLabel.getStyleClass().add("chapter-check");
                        xpBox.getChildren().addAll(checkLabel, xpText);
                } else {
                        xpBox.getChildren().addAll(xpLabel, xpText);
                }

                card.getChildren().addAll(iconCircle, infoBox, xpBox);

                if (!chapterDto.isLocked()) {
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
                        card.setOnMouseClicked(e -> showDetail(chapterDto, card));
                }

                return card;
        }

        private void showDetail(ChapterProgressDTO chapterDto, HBox card) {
                if (selectedCard != null) {
                        selectedCard.getStyleClass().remove("chapter-card-selected");
                }
                selectedCard = card;
                selectedChapter = chapterDto;
                card.getStyleClass().add("chapter-card-selected");

                if (chapterDto.getChapter().getIconImage() != null) {
                        try {
                                Image img = new Image(getClass().getResourceAsStream(chapterDto.getChapter().getIconImage()));
                                detailIconImage.setImage(img);
                        } catch (Exception e) {}
                }

                detailTitle.setText(chapterDto.getChapter().getTitle());
                detailDifficulty.setText(difficultyText(chapterDto.getChapter().getDifficulty()));
                detailDifficulty.getStyleClass().removeAll("diff-beginner", "diff-intermediate", "diff-advanced");
                detailDifficulty.getStyleClass().add(difficultyClass(chapterDto.getChapter().getDifficulty()));
                detailDescription.setText(chapterDto.getChapter().getDescription());
                detailLessons.setText(chapterDto.getCompletedLessons() + " / " + chapterDto.getChapter().getTotalLessons() + " lessons completed");
                detailXP.setText(chapterDto.getChapter().getXpReward() + " XP reward");

                if (chapterDto.isCompleted()) {
                        detailAction.setText("Completed");
                        detailAction.getStyleClass().removeAll("lp-detail-action");
                        detailAction.getStyleClass().add("lp-detail-action-done");
                } else {
                        int pct = (int) (chapterDto.getProgress() * 100);
                        detailAction.setText(pct > 0 ? "Continue (" + pct + "%)" : "Start chapter");
                        detailAction.getStyleClass().removeAll("lp-detail-action-done");
                        if (!detailAction.getStyleClass().contains("lp-detail-action")) {
                                detailAction.getStyleClass().add("lp-detail-action");
                        }
                }

                detailAction.setOnMouseClicked(e -> {
                        if (onStartChapter != null)
                                onStartChapter.accept(selectedChapter.getChapter());
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
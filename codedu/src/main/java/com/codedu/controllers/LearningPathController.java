package com.codedu.controllers;

import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.dtos.learning.ChapterDTO;
import com.codedu.models.learning.Chapter.Difficulty;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.ChapterService;
import com.codedu.services.interfaces.LearningPathService;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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

import java.util.List;
import java.util.function.Consumer;

@Controller
public class LearningPathController {

        @Autowired
        private LearningPathService learningPathService;

        @Autowired
        private ChapterService chapterService;

        @Autowired
        private UserRepository userRepository;

        @FXML private VBox pathContainer;
        @FXML private Label chapterCountLabel;

        @FXML private VBox detailPanel;
        @FXML private ImageView detailIconImage;
        @FXML private Label detailTitle;
        @FXML private Label detailDifficulty;
        @FXML private Label detailDescription;
        @FXML private Label detailLessons;
        @FXML private Label detailXP;
        @FXML private Label detailAction;

        private List<ChapterProgressDTO> chapters;
        private HBox selectedCard = null;
        private ChapterProgressDTO selectedChapter = null;

        // 🚀 Changed from Consumer<Chapter> to Consumer<ChapterDTO>
        private Consumer<ChapterDTO> onStartChapter;

        public void setOnStartChapter(Consumer<ChapterDTO> callback) {
                this.onStartChapter = callback;
        }

        @FXML
        public void initialize() {
                renderIfReady();
        }

        public void loadUserData(User currentUser) {
                if (currentUser == null || currentUser.getId() == 0) return;
                User attachedUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
                this.chapters = learningPathService.getOrCreateLearningPath(attachedUser);
                Platform.runLater(this::renderIfReady);
        }

        private void renderIfReady() {
                if (chapterCountLabel != null && pathContainer != null && chapters != null && !chapters.isEmpty()) {
                        chapterCountLabel.setText(chapters.size() + " Chapters");
                        buildPath();
                }
        }

        private void buildPath() {
                pathContainer.getChildren().clear();
                for (int i = 0; i < chapters.size(); i++) {
                        ChapterProgressDTO chapterDto = chapters.get(i);
                        if (i > 0) pathContainer.getChildren().add(buildConnector(chapterDto));

                        HBox card = buildChapterCard(chapterDto, i);

                        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
                        fade.setFromValue(0); fade.setToValue(1); fade.setDelay(Duration.millis(i * 80));

                        TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
                        slide.setFromY(20); slide.setToY(0); slide.setDelay(Duration.millis(i * 80));

                        card.setOpacity(0);
                        pathContainer.getChildren().add(card);
                        fade.play(); slide.play();
                }
        }

        private VBox buildConnector(ChapterProgressDTO nextChapter) {
                VBox connectorBox = new VBox();
                connectorBox.setAlignment(Pos.CENTER);
                connectorBox.setPrefHeight(36);
                Region line = new Region();
                line.getStyleClass().add("chapter-connector");
                if (nextChapter.isLocked()) line.getStyleClass().add("chapter-connector-locked");
                else if (nextChapter.isCompleted()) line.getStyleClass().add("chapter-connector-completed");
                line.setPrefWidth(3); line.setMaxWidth(3); line.setPrefHeight(36);
                VBox.setVgrow(line, Priority.ALWAYS);
                connectorBox.getChildren().add(line);
                return connectorBox;
        }

        private HBox buildChapterCard(ChapterProgressDTO chapterDto, int index) {
                HBox card = new HBox(16);
                card.getStyleClass().add("chapter-card");
                card.setPadding(new Insets(16, 20, 16, 16));
                card.setAlignment(Pos.CENTER_LEFT);

                if (chapterDto.isCompleted()) card.getStyleClass().add("chapter-card-completed");
                else if (chapterDto.isLocked()) card.getStyleClass().add("chapter-card-locked");

                StackPane iconCircle = new StackPane();
                iconCircle.getStyleClass().add("chapter-icon");
                if (chapterDto.isCompleted()) iconCircle.getStyleClass().add("chapter-icon-completed");
                else if (chapterDto.isLocked()) iconCircle.getStyleClass().add("chapter-icon-locked");
                iconCircle.setMinSize(52, 52);

                if (chapterDto.isLocked()) {
                        iconCircle.getChildren().add(new Label("🔒"));
                } else if (chapterDto.getChapter().iconImage() != null) {
                        try {
                                var imgUrl = getClass().getResource(chapterDto.getChapter().iconImage());
                                if (imgUrl != null) {
                                        Image img = new Image(imgUrl.toExternalForm());
                                        ImageView iv = new ImageView(img);
                                        iv.setFitWidth(40); iv.setFitHeight(40); iv.setPreserveRatio(true);
                                        iconCircle.getChildren().add(iv);
                                } else {
                                        iconCircle.getChildren().add(new Label("•"));
                                }
                        } catch (Exception e) { iconCircle.getChildren().add(new Label("•")); }
                } else {
                        iconCircle.getChildren().add(new Label("•"));
                }

                VBox infoBox = new VBox(4);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                HBox titleRow = new HBox(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);
                Label titleLabel = new Label(chapterDto.getChapter().title());
                titleLabel.getStyleClass().add("chapter-title");

                Label diffTag = new Label(difficultyText(chapterDto.getChapter().difficulty()));
                diffTag.getStyleClass().addAll("difficulty-tag", difficultyClass(chapterDto.getChapter().difficulty()));
                titleRow.getChildren().addAll(titleLabel, diffTag);

                Label descLabel = new Label(chapterDto.getChapter().description());
                descLabel.getStyleClass().add("chapter-desc");
                descLabel.setWrapText(true);

                ProgressBar progressBar = new ProgressBar(chapterDto.getProgress());
                progressBar.getStyleClass().add("chapter-progress-bar");
                progressBar.setPrefWidth(140);

                infoBox.getChildren().addAll(titleRow, descLabel, progressBar);

                VBox xpBox = new VBox(4);
                xpBox.setAlignment(Pos.CENTER);
                xpBox.setMinWidth(70);
                Label xpLabel = new Label("+" + chapterDto.getChapter().xpReward());
                xpLabel.getStyleClass().add("chapter-xp-value");
                xpBox.getChildren().addAll(xpLabel, new Label("XP"));

                card.getChildren().addAll(iconCircle, infoBox, xpBox);

                if (!chapterDto.isLocked()) {
                        card.setOnMouseClicked(e -> showDetail(chapterDto, card));
                        card.setOnMouseEntered(e -> { card.setScaleX(1.02); card.setScaleY(1.02); });
                        card.setOnMouseExited(e -> { card.setScaleX(1.0); card.setScaleY(1.0); });
                }
                return card;
        }

        private void showDetail(ChapterProgressDTO chapterDto, HBox card) {
                if (selectedCard != null) selectedCard.getStyleClass().remove("chapter-card-selected");
                selectedCard = card;
                selectedChapter = chapterDto;
                card.getStyleClass().add("chapter-card-selected");

                detailTitle.setText(chapterDto.getChapter().title());
                detailDescription.setText(chapterDto.getChapter().description());
                detailXP.setText(chapterDto.getChapter().xpReward() + " XP reward");

                if (detailIconImage != null && chapterDto.getChapter().iconImage() != null) {
                        try {
                                var imgUrl = getClass().getResource(chapterDto.getChapter().iconImage());
                                if (imgUrl != null) detailIconImage.setImage(new Image(imgUrl.toExternalForm()));
                        } catch (Exception ignored) {}
                }

                if (chapterDto.isCompleted()) {
                        detailAction.setText("Review Chapter");
                } else {
                        int pct = (int) (chapterDto.getProgress() * 100);
                        detailAction.setText(pct > 0 ? "Continue (" + pct + "%)" : "Start chapter");
                }

                detailAction.setOnMouseClicked(e -> {
                        if (onStartChapter != null) {
                                // Assuming chapterService returns the fully loaded ChapterDTO
                                chapterService.getChapterDtoWithQuestions((long) chapterDto.getChapter().id())
                                        .ifPresent(fullChapterDto -> onStartChapter.accept(fullChapterDto));
                        }
                });

                if (!detailPanel.isVisible()) {
                        detailPanel.setVisible(true);
                        detailPanel.setManaged(true);
                        TranslateTransition tt = new TranslateTransition(Duration.millis(300), detailPanel);
                        detailPanel.setTranslateX(280); tt.setToX(0); tt.play();
                }
        }

        private String difficultyText(Difficulty d) { return d == null ? "" : d.toString().toLowerCase(); }
        private String difficultyClass(Difficulty d) { return d == null ? "diff-beginner" : "diff-" + d.toString().toLowerCase(); }
}
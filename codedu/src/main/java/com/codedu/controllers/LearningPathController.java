package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.dtos.learning.ChapterDTO;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.ChapterService;
import com.codedu.services.interfaces.LearningPathService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
        @FXML private Label detailXP;
        @FXML private Button detailActionButton;

        private List<ChapterProgressDTO> chapters;
        private HBox selectedCard = null;
        private Consumer<ChapterDTO> onStartChapter;

        private final String LOGO_BLUE = "#00AEEF";
        private final String LOGO_ORANGE = "#F7941D";
        private final String COLOR_LOCKED = "#4c566a";

        public void setOnStartChapter(Consumer<ChapterDTO> callback) {
                this.onStartChapter = callback;
        }

        @FXML
        public void initialize() {
                if (detailActionButton != null) {
                        detailActionButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);

                        detailActionButton.setStyle("-fx-text-fill: " + LOGO_BLUE + "; -fx-border-color: " + LOGO_BLUE + ";");
                }
                renderIfReady();
        }

        public void loadUserData(User currentUser) {
                if (currentUser == null || currentUser.getId() == 0) {
                        return;
                }
                final int userId = currentUser.getId();
                CompletableFuture.runAsync(() -> {
                        try {
                                User attachedUser = userRepository.findById(userId).orElse(currentUser);
                                List<ChapterProgressDTO> path = learningPathService.getOrCreateLearningPath(attachedUser);
                                Platform.runLater(() -> {
                                        this.chapters = path;
                                        renderIfReady();
                                });
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                });
        }

        private void renderIfReady() {
                if (pathContainer != null && chapters != null && !chapters.isEmpty()) {
                        if (chapterCountLabel != null) chapterCountLabel.setText(chapters.size() + " Chapters");
                        buildPath();
                }
        }

        private void buildPath() {
                pathContainer.getChildren().clear();
                for (int i = 0; i < chapters.size(); i++) {
                        ChapterProgressDTO chapterDto = chapters.get(i);

                        if (i > 0) {
                                Region line = new Region();
                                line.setPrefSize(4, 32);
                                line.setMaxWidth(4);
                                String lineColor = chapterDto.isLocked() ? COLOR_LOCKED : LOGO_BLUE;
                                line.setStyle("-fx-background-color: " + lineColor + "; -fx-opacity: 0.6;");
                                pathContainer.getChildren().add(line);
                        }

                        // Chapter Card
                        final HBox card = buildChapterCard(chapterDto);
                        pathContainer.getChildren().add(card);

                        FadeTransition ft = new FadeTransition(Duration.millis(300), card);
                        ft.setFromValue(0.0); ft.setToValue(1.0);
                        ft.play();
                }
        }

        private HBox buildChapterCard(final ChapterProgressDTO chapterDto) {
                final HBox card = new HBox(20);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(20));
                card.setPrefWidth(550);
                card.setMaxWidth(550);
                card.setStyle("-fx-background-color: #3b4252; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-border-color: #4c566a;");

                StackPane iconBox = new StackPane();
                iconBox.setPrefSize(50, 50);
                String iconBg = chapterDto.isLocked() ? "#434c5e" : LOGO_BLUE;
                iconBox.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 25;");
                Label iconLabel = new Label(chapterDto.isLocked() ? "🔒" : "📖");
                iconLabel.setStyle("-fx-font-size: 20; -fx-text-fill: white;");
                iconBox.getChildren().add(iconLabel);

                VBox info = new VBox(6);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label title = new Label(chapterDto.getChapter().title());
                title.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold; -fx-font-size: 16px;");

                ProgressBar pb = new ProgressBar(chapterDto.getProgress());
                pb.setPrefWidth(180);
                pb.setPrefHeight(14);

                pb.setStyle(
                        "-fx-accent: " + LOGO_BLUE + "; " +
                                "-fx-background-radius: 50; " + // High radius for oval shape
                                "-fx-border-radius: 50; " +
                                "-fx-border-color: rgba(255, 255, 255, 0.4); " + // More visible border
                                "-fx-border-width: 1.5; " +
                                "-fx-control-inner-background: #2e3440; " +
                                "-fx-padding: 1;"
                );

                info.getChildren().addAll(title, pb);

                Label xp = new Label("+" + chapterDto.getChapter().xpReward() + " XP");
                xp.setStyle("-fx-font-weight: bold; -fx-text-fill: " + LOGO_BLUE + ";");

                card.getChildren().addAll(iconBox, info, xp);

                if (!chapterDto.isLocked()) {
                        card.setCursor(Cursor.HAND);
                        card.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                        showDetail(chapterDto, card);
                                }
                        });
                } else {
                        card.setOpacity(0.5);
                }
                return card;
        }

        private void showDetail(final ChapterProgressDTO chapterDto, final HBox card) {
                if (selectedCard != null) {
                        selectedCard.setStyle("-fx-background-color: #3b4252; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-border-color: #4c566a;");
                }

                selectedCard = card;
                card.setStyle("-fx-background-color: #3b4252; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-width: 2.5; -fx-border-color: " + LOGO_BLUE + ";");

                detailTitle.setText(chapterDto.getChapter().title());
                detailTitle.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold; -fx-font-size: 20;");

                detailDescription.setText(chapterDto.getChapter().description());
                detailXP.setText(chapterDto.getChapter().xpReward() + " XP Reward");
                detailDifficulty.setText("Level: " + chapterDto.getChapter().difficulty());

                detailDifficulty.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-size: 12px;");
                detailXP.setStyle("-fx-font-weight: bold; -fx-text-fill: " + LOGO_BLUE + ";");

                if (chapterDto.isCompleted()) {
                        detailActionButton.setText("Review Chapter");
                } else {
                        int pct = (int) (chapterDto.getProgress() * 100);
                        detailActionButton.setText(pct > 0 ? "Continue (" + pct + "%)" : "Start Chapter");
                }

                detailActionButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                                if (onStartChapter != null) {
                                        Optional<ChapterDTO> full = chapterService.getChapterDtoWithQuestions((long) chapterDto.getChapter().id());
                                        if (full.isPresent()) {
                                                onStartChapter.accept(full.get());
                                        }
                                }
                        }
                });

                detailPanel.setVisible(true);
                detailPanel.setManaged(true);
        }
}
package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.models.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.codedu.models.gamification.Achievement;
import com.codedu.repositories.interfaces.AchievementRepository;
import com.codedu.services.interfaces.AchievementEvaluationService;
import java.util.List;

@Controller
public class AchievementsController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox achievementList;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private AchievementEvaluationService achievementEvaluationService;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        buildAchievements();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        buildAchievements();
    }

    private void buildAchievements() {
        if (achievementList == null || currentUser == null) {
            return;
        }
        achievementList.getChildren().clear();

        List<Achievement> allAchievements = achievementRepository.getAll();

        final String baseStyle = "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; " +
                "-fx-border-color: -color-border-default;";

        final String hoverStyle = "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; "
                + "-fx-border-color: -color-accent-emphasis;";

        for (int i = 0; i < allAchievements.size(); i++) {
            Achievement a = allAchievements.get(i);

            final HBox goalCard = new HBox(20);
            goalCard.setAlignment(Pos.CENTER_LEFT);
            goalCard.setPadding(new Insets(20));
            goalCard.getStyleClass().add(Styles.BG_DEFAULT);
            goalCard.setStyle(baseStyle);

            StackPane badgeContainer = new StackPane();
            badgeContainer.setMinWidth(80);
            badgeContainer.setMinHeight(80);
            badgeContainer.setMaxWidth(80);
            badgeContainer.setMaxHeight(80);

            badgeContainer.setStyle(
                    "-fx-background-color: -color-bg-subtle; " + "-fx-background-radius: 40; "
                            + "-fx-border-color: -color-border-muted; " + "-fx-border-radius: 40; "
                            + "-fx-border-width: 1;");

            Label placeholderText = new Label("BADGE");
            placeholderText.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
            badgeContainer.getChildren().add(placeholderText);

            VBox textContainer = new VBox(6);
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            Label goalTitle = new Label(a.getName());
            goalTitle.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);

            double progressVal = achievementEvaluationService.getProgressPercentage(a, currentUser);
            boolean isCompleted = progressVal >= 1.0;

            if (isCompleted) {
                goalTitle.setStyle("-fx-text-fill: -color-success-emphasis;");
            } else {
                goalTitle.setStyle("-fx-text-fill: #00ADEF;");
            }

            Label goalMeta = new Label(isCompleted ? "COMPLETED" : "COMPLETION GOAL");
            if (isCompleted) {
                goalMeta.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_CAPTION, Styles.SUCCESS);
            } else {
                goalMeta.setStyle("-fx-text-fill: #D9822B; -fx-font-weight: bold; -fx-font-size: 11px;");
                goalMeta.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_CAPTION);
            }

            Label goalBody = new Label(a.getCriteria());
            goalBody.setWrapText(true);
            goalBody.getStyleClass().add(Styles.TEXT_MUTED);

            String textProgress = achievementEvaluationService.getProgressText(a, currentUser);
            Label goalProgressText = new Label(textProgress);
            goalProgressText.getStyleClass().add(Styles.TEXT_CAPTION);

            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(progressVal);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.getStyleClass().add(Styles.SMALL);
            progressBar.setStyle("-fx-min-height: 12; " + "-fx-max-height: 12; " +
                    "-fx-background-radius: 10; " + "-fx-border-radius: 10;");
            if (isCompleted) {
                progressBar.getStyleClass().add(Styles.SUCCESS);
            }

            textContainer.getChildren().addAll(goalTitle, goalMeta, goalBody, goalProgressText, progressBar);


            goalCard.setOnMouseEntered(new javafx.event.EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent event) {
                    goalCard.setStyle(hoverStyle);
                }
            });

            goalCard.setOnMouseExited(new javafx.event.EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent event) {
                    goalCard.setStyle(baseStyle);
                }
            });

            goalCard.getChildren().addAll(badgeContainer, textContainer);
            achievementList.getChildren().add(goalCard);
        }
    }
}
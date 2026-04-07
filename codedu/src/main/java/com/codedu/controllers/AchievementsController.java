package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.gamification.AchievementProgressSnapshot;
import com.codedu.models.gamification.Achievement;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.AchievementEvaluationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@Scope("prototype")
public class AchievementsController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox achievementList;

    @Autowired
    private AchievementEvaluationService achievementEvaluationService;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadAchievementsAsync();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (achievementList != null) {
            achievementList.getChildren().clear();
            Label loading = new Label("Loading achievements…");
            loading.setStyle("-fx-text-fill: #88c0d0; -fx-font-size: 14px;");
            achievementList.getChildren().add(loading);
        }
    }

    private void loadAchievementsAsync() {
        if (achievementList == null || currentUser == null || currentUser.getId() <= 0) {
            return;
        }
        final int userId = currentUser.getId();
        achievementList.getChildren().clear();
        Label loading = new Label("Loading achievements…");
        loading.setStyle("-fx-text-fill: #88c0d0; -fx-font-size: 14px;");
        achievementList.getChildren().add(loading);

        CompletableFuture.supplyAsync(() -> achievementEvaluationService.loadAllProgressSnapshots(userId))
                .whenComplete((snapshots, ex) -> Platform.runLater(() -> {
                    if (ex != null) {
                        ex.printStackTrace();
                        achievementList.getChildren().clear();
                        Label err = new Label("Could not load achievements.");
                        err.setStyle("-fx-text-fill: #bf616a;");
                        achievementList.getChildren().add(err);
                        return;
                    }
                    renderAchievements(snapshots);
                }));
    }

    private void renderAchievements(List<AchievementProgressSnapshot> snapshots) {
        if (achievementList == null) {
            return;
        }
        achievementList.getChildren().clear();

        final String baseStyle = "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; "
                + "-fx-border-color: -color-border-default;";

        final String hoverStyle = "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; "
                + "-fx-border-color: -color-accent-emphasis;";

        for (AchievementProgressSnapshot snap : snapshots) {
            Achievement a = snap.achievement();
            double progressVal = snap.progress();
            boolean isCompleted = progressVal >= 1.0;

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

            Label goalProgressText = new Label(snap.progressText());
            goalProgressText.getStyleClass().add(Styles.TEXT_CAPTION);

            ProgressBar progressBar = new ProgressBar(progressVal);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.getStyleClass().add(Styles.SMALL);
            progressBar.setStyle("-fx-min-height: 12; " + "-fx-max-height: 12; "
                    + "-fx-background-radius: 10; " + "-fx-border-radius: 10;");
            if (isCompleted) {
                progressBar.getStyleClass().add(Styles.SUCCESS);
            }

            textContainer.getChildren().addAll(goalTitle, goalMeta, goalBody, goalProgressText, progressBar);

            goalCard.setOnMouseEntered(event -> goalCard.setStyle(hoverStyle));
            goalCard.setOnMouseExited(event -> goalCard.setStyle(baseStyle));

            goalCard.getChildren().addAll(badgeContainer, textContainer);
            achievementList.getChildren().add(goalCard);
        }
    }
}

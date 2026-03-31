package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.models.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.codedu.models.gamification.Achievement;
import com.codedu.repositories.interfaces.AchievementRepository;
import com.codedu.services.AchievementEvaluationService;
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
        // Passive evaluateAndGrantAchievements was removed so users must manually claim
        // rewards.
        buildAchievements();
    }

    public void setLeaderboard(LeaderBoard leaderboard) {
        // No longer using the demo leaderboard logic
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

        List<Achievement> allAchievements = achievementEvaluationService.getAllAchievementsWithBadges();

        final String baseStyle = "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; " +
                "-fx-border-color: -color-border-default;";

        final String hoverStyle = "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; "
                +
                "-fx-border-color: -color-accent-emphasis;";

        for (int i = 0; i < allAchievements.size(); i++) {
            Achievement a = allAchievements.get(i);

            final javafx.scene.layout.HBox goalCard = new javafx.scene.layout.HBox(20);
            goalCard.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            goalCard.setPadding(new javafx.geometry.Insets(20));
            goalCard.getStyleClass().add(Styles.BG_DEFAULT);
            goalCard.setStyle(baseStyle);

            javafx.scene.layout.StackPane badgeContainer = new javafx.scene.layout.StackPane();
            badgeContainer.setMinWidth(80);
            badgeContainer.setMinHeight(80);
            badgeContainer.setMaxWidth(80);
            badgeContainer.setMaxHeight(80);

            badgeContainer.setStyle(
                    "-fx-background-color: -color-bg-subtle; " + "-fx-background-radius: 40; "
                            + "-fx-border-color: -color-border-muted; " + "-fx-border-radius: 40; "
                            + "-fx-border-width: 1;");

            boolean alreadyClaimed = false;
            if (currentUser != null && currentUser.getGameState() != null
                    && currentUser.getGameState().getAchievements() != null) {
                for (com.codedu.models.gamification.Achievement earned : currentUser.getGameState().getAchievements()) {
                    if (earned.getId() == a.getId()) {
                        alreadyClaimed = true;
                        break;
                    }
                }
            }

            double progressVal = achievementEvaluationService.getProgressPercentage(a, currentUser);
            boolean isCompleted = progressVal >= 1.0;

            javafx.scene.image.ImageView badgeIcon = new javafx.scene.image.ImageView();
            badgeIcon.setFitWidth(50);
            badgeIcon.setFitHeight(50);
            badgeIcon.setPreserveRatio(true);

            try {
                String rawPath = a.getBadge().getIconURL();
                if (rawPath == null)
                    rawPath = "badge.png";
                String fileName = java.nio.file.Paths.get(rawPath).getFileName().toString();
                String iconPath = "/assets/badges/" + fileName;

                java.net.URL resource = getClass().getResource(iconPath);
                if (resource != null) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(resource.toExternalForm());
                    badgeIcon.setImage(img);
                    if (!alreadyClaimed && !isCompleted) {
                        // Apply a grayscale or dark effect for locked achievements!
                        javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
                        colorAdjust.setSaturation(-1.0);
                        colorAdjust.setBrightness(-0.5);
                        badgeIcon.setEffect(colorAdjust);
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load badge image: " + e.getMessage());
            }

            badgeContainer.getChildren().add(badgeIcon);

            javafx.scene.layout.VBox textContainer = new javafx.scene.layout.VBox(6);
            javafx.scene.layout.HBox.setHgrow(textContainer, javafx.scene.layout.Priority.ALWAYS);

            String nameText = a.getName();

            Label goalTitle = new Label(nameText);
            goalTitle.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);

            if (alreadyClaimed) {
                goalTitle.setStyle("-fx-text-fill: -color-success-emphasis;");
            } else if (isCompleted) {
                goalTitle.setStyle("-fx-text-fill: #F1C40F;"); // Gold/Yellow indicating ready to claim
            } else {
                goalTitle.setStyle("-fx-text-fill: #E67E22;"); // Standard active orange
            }

            String metaText = alreadyClaimed ? "CLAIMED" : (isCompleted ? "READY TO CLAIM!" : "COMPLETION GOAL");
            Label goalMeta = new Label(metaText);
            goalMeta.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_CAPTION,
                    alreadyClaimed ? Styles.SUCCESS : (isCompleted ? Styles.WARNING : Styles.DANGER));

            Label goalBody = new Label(a.getCriteria());
            goalBody.setWrapText(true);
            goalBody.getStyleClass().add(Styles.TEXT_MUTED);

            String textProgress = achievementEvaluationService.getProgressText(a, currentUser);
            Label goalProgressText = new Label(textProgress);
            goalProgressText.getStyleClass().add(Styles.TEXT_CAPTION);

            textContainer.getChildren().addAll(goalTitle, goalMeta, goalBody, goalProgressText);

            if (!alreadyClaimed && isCompleted) {
                javafx.scene.control.Button claimBtn = new javafx.scene.control.Button("Claim Reward (XP & Tokens)");
                claimBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS);
                claimBtn.setOnAction(e -> {
                    boolean success = achievementEvaluationService.claimAchievement(currentUser, a.getId());
                    if (success) {
                        buildAchievements(); // Refresh list to update UI
                    }
                });
                textContainer.getChildren().add(claimBtn);
            } else {
                javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(progressVal);
                progressBar.setMaxWidth(Double.MAX_VALUE);
                progressBar.getStyleClass().add(Styles.SMALL);
                progressBar.setStyle("-fx-min-height: 12; " + "-fx-max-height: 12; " +
                        "-fx-background-radius: 10; " + "-fx-border-radius: 10;");
                if (alreadyClaimed) {
                    progressBar.getStyleClass().add(Styles.SUCCESS);
                }
                textContainer.getChildren().add(progressBar);
            }

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

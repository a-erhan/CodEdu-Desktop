package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.models.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

@Controller
public class AchievementsController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox achievementList;

    private LeaderBoard leaderboard;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        ensureDemoLeaderboard();
        buildAchievements();
    }

    public void setLeaderboard(LeaderBoard leaderboard) {
        this.leaderboard = leaderboard;
        ensureDemoLeaderboard();
        buildAchievements();
    }

    /** Create demo leaderboard in this controller if none set (demo data lives here). */
    private void ensureDemoLeaderboard() {
        if (leaderboard != null || currentUser == null) return;
        Competitor me = Competitor.builder()
                .user(currentUser)
                .rankingPoint(2180)
                .totalWins(12)
                .totalLosses(8)
                .totalMatches(20)
                .build();
        Competitor c1 = Competitor.builder().rankingPoint(2840).totalWins(20).totalLosses(5).totalMatches(25).build();
        Competitor c2 = Competitor.builder().rankingPoint(2650).totalWins(18).totalLosses(6).totalMatches(24).build();
        Competitor c3 = Competitor.builder().rankingPoint(2420).totalWins(15).totalLosses(7).totalMatches(22).build();
        Competitor c5 = Competitor.builder().rankingPoint(1950).totalWins(10).totalLosses(9).totalMatches(19).build();
        leaderboard = LeaderBoard.builder()
                .name("Weekly XP")
                .userRank(4)
                .requiredLevel(1)
                .build();
        leaderboard.addCompetitor(c1);
        leaderboard.addCompetitor(c2);
        leaderboard.addCompetitor(c3);
        leaderboard.addCompetitor(me);
        leaderboard.addCompetitor(c5);
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        buildAchievements();
    }

    private void buildAchievements() {
        ensureDemoLeaderboard();
        if (achievementList == null || leaderboard == null) {
            return;
        }
        achievementList.getChildren().clear();

        final String baseStyle =
                "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; " +
                        "-fx-border-color: -color-border-default;";

        final String hoverStyle =
                "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-border-width: 2.5; " +
                        "-fx-border-color: -color-accent-emphasis;";

        for (int i = 0; i < leaderboard.getCompetitors().size(); i++) {
            Competitor c = leaderboard.getCompetitors().get(i);

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
                            + "-fx-border-color: -color-border-muted; " + "-fx-border-radius: 40; " + "-fx-border-width: 1;"
            );


            javafx.scene.image.ImageView badgeIcon = new javafx.scene.image.ImageView();
            badgeIcon.setFitWidth(50);
            badgeIcon.setFitHeight(50);
            badgeIcon.setPreserveRatio(true);

            // when we use icon url, use this after cleaning the placeholder:
            // badgeIcon.setImage(new javafx.scene.image.Image(c.getBadge().getIconURL()));

            Label placeholderText = new Label("CODE");
            placeholderText.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
            badgeContainer.getChildren().add(placeholderText);

            javafx.scene.layout.VBox textContainer = new javafx.scene.layout.VBox(6);
            javafx.scene.layout.HBox.setHgrow(textContainer, javafx.scene.layout.Priority.ALWAYS);

            String nameText = "Achievement " + (i + 1);
            if (c.getUser() != null && c.getUser().getUsername() != null) {
                nameText = c.getUser().getUsername();
            }
            Label goalTitle = new Label(nameText);
            goalTitle.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);

            goalTitle.setStyle("-fx-text-fill: #E67E22;");

            Label goalMeta = new Label("COMPLETION GOAL");
            goalMeta.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_CAPTION, Styles.SUCCESS);

            Label goalBody = new Label("Reach " + c.getRankingPoint() + " XP with at least "
                    + String.format("%.0f", c.getWinRate()) + "% win rate.");
            goalBody.setWrapText(true);
            goalBody.getStyleClass().add(Styles.TEXT_MUTED);

            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(0.65);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.getStyleClass().add(Styles.SMALL);
            progressBar.setStyle("-fx-min-height: 12; " + "-fx-max-height: 12; " +
                    "-fx-background-radius: 10; " + "-fx-border-radius: 10;");

            textContainer.getChildren().addAll(goalTitle, goalMeta, goalBody, progressBar);

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


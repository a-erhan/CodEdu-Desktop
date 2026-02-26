package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Competitor;
import com.codedu.models.LeaderBoard;
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

    public void setLeaderboard(LeaderBoard leaderboard) {
        this.leaderboard = leaderboard;
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
        if (achievementList == null || leaderboard == null) {
            return;
        }
        achievementList.getChildren().clear();

        for (int i = 0; i < leaderboard.getCompetitors().size(); i++) {
            Competitor c = leaderboard.getCompetitors().get(i);

            VBox goalCard = new VBox(6);
            goalCard.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            goalCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            goalCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            String nameText = c.getUser() != null && c.getUser().getUsername() != null
                    ? c.getUser().getUsername()
                    : "Goal " + (i + 1);

            Label goalTitle = new Label(nameText);
            goalTitle.getStyleClass().add(Styles.TEXT_BOLD);

            Label goalBody = new Label("Reach " + c.getRankingPoint() + " XP with at least "
                    + String.format("%.0f", c.getWinRate()) + "% win rate.");
            goalBody.setWrapText(true);

            Label goalMeta = new Label("Progress goal");
            goalMeta.getStyleClass().add(Styles.TEXT_SUBTLE);

            goalCard.getChildren().addAll(goalTitle, goalBody, goalMeta);
            achievementList.getChildren().add(goalCard);
        }
    }
}


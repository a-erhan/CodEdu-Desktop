package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Competitor;
import com.codedu.models.LeaderBoard;
import com.codedu.models.User;
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


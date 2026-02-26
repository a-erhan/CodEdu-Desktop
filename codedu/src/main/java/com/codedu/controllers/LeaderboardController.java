package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Competitor;
import com.codedu.models.LeaderBoard;
import com.codedu.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.function.BiConsumer;

@Controller
public class LeaderboardController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox myCard;
    @FXML
    private Label myTitle;
    @FXML
    private Label myRankLabel;
    @FXML
    private Label myScoreLabel;

    @FXML
    private VBox boardList;

    private LeaderBoard leaderboard;
    private User currentUser;
    private BiConsumer<Competitor, List<Competitor>> onOpenProfile;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        ensureDemoLeaderboard();
        buildLeaderboard();
    }

    public void setLeaderboard(LeaderBoard leaderboard) {
        this.leaderboard = leaderboard;
        ensureDemoLeaderboard();
        buildLeaderboard();
    }

    public void setOnOpenProfile(BiConsumer<Competitor, List<Competitor>> onOpenProfile) {
        this.onOpenProfile = onOpenProfile;
        buildLeaderboard();
    }

    /** Create demo leaderboard in this controller if none set (demo data lives here). */
    private void ensureDemoLeaderboard() {
        if (leaderboard != null) return;
        if (currentUser == null) return;
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
        buildLeaderboard();
    }

    private void buildLeaderboard() {
        ensureDemoLeaderboard();
        if (leaderboard == null || boardList == null || myCard == null) {
            return;
        }

        // Determine current user's competitor
        java.util.List<Competitor> competitors = leaderboard.getCompetitors();
        int myRank = leaderboard.getUserRank();
        int total = competitors.size();
        int myIndex = Math.max(0, Math.min(total - 1, myRank - 1));
        Competitor me = competitors.get(myIndex);

        // Style and populate "my position" card
        myCard.getChildren().clear();
        myCard.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        myCard.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));
        myCard.getStyleClass().addAll(
                Styles.BORDERED,
                Styles.ROUNDED,
                Styles.BG_ACCENT_SUBTLE,
                Styles.ELEVATED_1
        );

        String myName = me.getUser() != null && me.getUser().getUsername() != null
                ? me.getUser().getUsername()
                : "You";

        Label myTitleLabel = new Label("Your position");
        myTitleLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Label myRankText = new Label("#" + myRank + " of " + total + " · " + myName);
        myRankText.getStyleClass().add(Styles.TITLE_3);

        Label myScoreText = new Label(
                me.getRankingPoint() + " XP · " + String.format("%.0f", me.getWinRate()) + "% wins");
        myScoreText.getStyleClass().add(Styles.TEXT_SUBTLE);

        myCard.getChildren().addAll(myTitleLabel, myRankText, myScoreText);

        // Build full leaderboard list
        boardList.getChildren().clear();

        for (int i = 0; i < competitors.size(); i++) {
            Competitor c = competitors.get(i);

            HBox line = new HBox(12);
            line.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            line.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));

            if (i == myIndex) {
                line.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_ACCENT_SUBTLE);
            } else {
                line.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
            }

            Label pos = new Label(String.valueOf(i + 1));
            pos.getStyleClass().add(Styles.TEXT_BOLD);

            String nameText = c.getUser() != null && c.getUser().getUsername() != null
                    ? c.getUser().getUsername()
                    : "Player " + (i + 1);
            Label name = new Label(nameText);

            if (onOpenProfile != null) {
                name.getStyleClass().add(Styles.INTERACTIVE);
                java.util.List<Competitor> list = leaderboard.getCompetitors();
                name.setOnMouseClicked(e -> onOpenProfile.accept(c, list));
            }

            Label score = new Label(
                    c.getRankingPoint() + " XP · " + String.format("%.0f", c.getWinRate()) + "% wins");
            score.getStyleClass().add(Styles.TEXT_SUBTLE);

            line.getChildren().addAll(pos, name, score);
            boardList.getChildren().add(line);
        }
    }
}


package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Competitor;
import com.codedu.models.LeaderBoard;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.function.Consumer;

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
    private Consumer<Competitor> onOpenProfile;

    public void setLeaderboard(LeaderBoard leaderboard) {
        this.leaderboard = leaderboard;
        buildLeaderboard();
    }

    public void setOnOpenProfile(Consumer<Competitor> onOpenProfile) {
        this.onOpenProfile = onOpenProfile;
        // Rebuild so existing name labels get wired with click handlers
        buildLeaderboard();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        buildLeaderboard();
    }

    private void buildLeaderboard() {
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
                name.setOnMouseClicked(e -> onOpenProfile.accept(c));
            }

            Label score = new Label(
                    c.getRankingPoint() + " XP · " + String.format("%.0f", c.getWinRate()) + "% wins");
            score.getStyleClass().add(Styles.TEXT_SUBTLE);

            line.getChildren().addAll(pos, name, score);
            boardList.getChildren().add(line);
        }
    }
}


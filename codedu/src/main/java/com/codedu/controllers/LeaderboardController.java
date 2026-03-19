package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.models.user.User;
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

        java.util.List<Competitor> competitors = leaderboard.getCompetitors();
        int myRank = leaderboard.getUserRank();
        int total = competitors.size();
        int myIndex = Math.max(0, Math.min(total - 1, myRank - 1));
        Competitor me = competitors.get(myIndex);

        myCard.getChildren().clear();
        myCard.setAlignment(javafx.geometry.Pos.CENTER);
        myCard.setPadding(new javafx.geometry.Insets(20));
        myCard.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");
        myCard.getStyleClass().addAll(
                Styles.BORDERED,
                Styles.ROUNDED,
                Styles.BG_ACCENT_SUBTLE,
                Styles.ELEVATED_1
        );

        VBox gapContainer = new VBox(5);
        gapContainer.setAlignment(javafx.geometry.Pos.CENTER);

        if (myIndex > 0) {
            Competitor playerAbove = competitors.get(myIndex - 1);
            int gap = playerAbove.getRankingPoint() - me.getRankingPoint();

            Label gapLabel = new Label(gap + " XP to reach #" + myIndex);
            gapLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px; -fx-font-style: italic;");

            javafx.scene.control.ProgressBar gapBar = new javafx.scene.control.ProgressBar();
            gapBar.setPrefWidth(180);
            gapBar.setMaxHeight(6);

            double progress = (double) me.getRankingPoint() / playerAbove.getRankingPoint();
            gapBar.setProgress(progress);

            gapBar.getStyleClass().add(Styles.MEDIUM);

            gapContainer.getChildren().addAll(gapLabel, gapBar);
        } else {
            Label topLabel = new Label("You're the leader!");
            topLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; -fx-font-weight: bold;");
            gapContainer.getChildren().add(topLabel);
        }

        String myName = me.getUser() != null && me.getUser().getUsername() != null
                ? me.getUser().getUsername()
                : "You";

        Label myTitleLabel = new Label("Your position:");
        myTitleLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Label myRankText = new Label("#" + myRank + " of " + total);
        myRankText.getStyleClass().add(Styles.TITLE_3);
        myRankText.setAlignment(javafx.geometry.Pos.CENTER);

        HBox myStatsBox = new HBox(10);
        myStatsBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label myXp = new Label(me.getRankingPoint() + " XP");
        myXp.setStyle("-fx-font-weight: bold;");

        Label mySep = new Label("|");

        Label myWin = new Label(String.format("%.0f", me.getWinRate()) + "% Win Rate");
        myWin.setStyle("-fx-font-weight: bold;");

        myStatsBox.getChildren().addAll(myXp, mySep, myWin);
        myCard.getChildren().addAll(myTitleLabel, myRankText, gapContainer, myStatsBox);

        boardList.getChildren().clear();

        for (int i = 0; i < competitors.size(); i++) {
            Competitor c = competitors.get(i);

            HBox line = new HBox(15);
            line.setAlignment(javafx.geometry.Pos.CENTER_LEFT); // Change to CENTER_LEFT
            line.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
            line.setMaxWidth(Double.MAX_VALUE);
            javafx.scene.layout.HBox.setHgrow(line, javafx.scene.layout.Priority.ALWAYS);

            String baseStyle = "-fx-background-radius: 15; -fx-border-radius: 15;";
            if (i == myIndex) {
                baseStyle += "-fx-border-color: #ccbdbb; -fx-border-width: 1.5;";
                line.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_ACCENT_SUBTLE);
            } else {
                line.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
                if (i % 2 != 0) {
                    baseStyle += "-fx-background-color: rgba(255, 255, 255, 0.03);";
                }
            }

            line.setStyle(baseStyle);
            final String finalBaseStyle = baseStyle;


            int levelVal = (c.getRankingPoint() / 1000) + 1;
            Label levelBadge = new Label("Lvl " + levelVal);
            levelBadge.setStyle(
                    "-fx-background-color: #34495e; " +
                            "-fx-text-fill: #ecf0f1; " +
                            "-fx-padding: 2 8 2 8; " +
                            "-fx-background-radius: 10; " +
                            "-fx-font-size: 9px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-opacity: 0.8;"
            );

            line.setOnMouseEntered(new javafx.event.EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent e) {
                    line.setStyle(finalBaseStyle + "-fx-background-color: #384351; -fx-cursor: hand;");
                    line.setScaleX(1.01);
                    line.setScaleY(1.01);
                }
            });

            line.setOnMouseExited(new javafx.event.EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent e) {
                    line.setStyle(finalBaseStyle);
                    line.setScaleX(1.0);
                    line.setScaleY(1.0);
                }
            });

            Label pos = new Label("#" + String.valueOf(i + 1));
            pos.getStyleClass().add(Styles.TEXT_BOLD);

            Label posIcon = new Label();
            posIcon.setMinWidth(30);
            if (i == 0) {
                pos.setStyle("-fx-text-fill: #FFD700;");
            }
            else if (i == 1) {
                pos.setStyle("-fx-text-fill: #C0C0C0;");
            }
            else if (i == 2) {
                pos.setStyle("-fx-text-fill: #CD7F32;");
            }

            String nameText = c.getUser() != null && c.getUser().getUsername() != null
                    ? c.getUser().getUsername()
                    : "Player " + (i + 1);
            Label name = new Label(nameText);
            name.setStyle("-fx-font-weight: bold;");

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label xpLabel = new Label(c.getRankingPoint() + " XP");
            xpLabel.setStyle("-fx-text-fill: #2775b1; -fx-font-weight: bold;");

            Label sep = new Label("|");

            Label winLabel = new Label(String.format("%.0f", c.getWinRate()) + "% Win Rate");
            winLabel.setStyle("-fx-text-fill: #308f5a; -fx-font-weight: bold;");

            line.getChildren().clear();
            line.getChildren().addAll(pos, name, levelBadge, spacer, xpLabel, new Label("|"), winLabel);
            boardList.getChildren().add(line);
        }
    }
}


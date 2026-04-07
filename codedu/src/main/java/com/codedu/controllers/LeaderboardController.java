package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.LeaderBoardService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Controller
@Scope("prototype")
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
    @FXML
    private ComboBox<String> scopeComboBox;

    private LeaderBoard leaderboard;
    @Autowired
    private LeaderBoardService leaderboardService;
    private User currentUser;
    private BiConsumer<Competitor, List<Competitor>> onOpenProfile;
    private boolean shellReady;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        tryScheduleInitialLoad();
    }

    public void setLeaderboard(LeaderBoard leaderboard) {
        this.leaderboard = leaderboard;
        buildLeaderboard();
    }

    public void setOnOpenProfile(BiConsumer<Competitor, List<Competitor>> onOpenProfile) {
        this.onOpenProfile = onOpenProfile;
        this.shellReady = true;
        tryScheduleInitialLoad();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (boardList != null) {
            boardList.setAlignment(Pos.TOP_CENTER);
        }
        if (scopeComboBox != null) {
            scopeComboBox.getItems().addAll("Weekly", "Monthly", "All-Time");
            scopeComboBox.setValue("Weekly");

            scopeComboBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String selectedScope = scopeComboBox.getValue();
                    fetchLeaderboardDataAsync(selectedScope);
                }
            });
        }
        showLoadingPlaceholder();
    }

    private void tryScheduleInitialLoad() {
        if (!shellReady || currentUser == null || scopeComboBox == null) {
            return;
        }
        fetchLeaderboardDataAsync(scopeComboBox.getValue());
    }

    private void showLoadingPlaceholder() {
        if (boardList == null) {
            return;
        }
        boardList.getChildren().clear();
        Label loading = new Label("Loading leaderboard…");
        loading.setStyle("-fx-text-fill: #88c0d0; -fx-font-size: 14px;");
        boardList.getChildren().add(loading);
    }

    private void fetchLeaderboardDataAsync(String scope) {
        if (scope == null || scope.isBlank()) {
            scope = "Weekly";
        }
        final String scopeFinal = scope;
        showLoadingPlaceholder();
        CompletableFuture.supplyAsync(() -> leaderboardService.getLeaderboardEntityByName(scopeFinal))
                .whenComplete((lb, ex) -> Platform.runLater(() -> {
                    if (ex != null) {
                        ex.printStackTrace();
                        if (boardList != null) {
                            boardList.getChildren().clear();
                            Label err = new Label("Could not load leaderboard.");
                            err.setStyle("-fx-text-fill: #bf616a;");
                            boardList.getChildren().add(err);
                        }
                        return;
                    }
                    this.leaderboard = lb;
                    updateSubtitle(scopeFinal);
                    buildLeaderboard();
                }));
    }

    private void updateSubtitle(String scope) {
        if (subtitleLabel == null) {
            return;
        }
        String s = scope == null || scope.isBlank() ? "All-Time" : scope.trim();
        if ("Weekly".equalsIgnoreCase(s)) {
            subtitleLabel.setText("Active in the last 7 days — ranked by level, then XP.");
        } else if ("Monthly".equalsIgnoreCase(s)) {
            subtitleLabel.setText("Active in the last 30 days — ranked by level, then XP.");
        } else {
            subtitleLabel.setText("All accounts — ranked by level, then XP.");
        }
    }

    private static String formatProgression(Competitor c) {
        if (c.getUser() != null && c.getUser().getGameState() != null) {
            var gs = c.getUser().getGameState();
            return "Lv " + gs.getLevel() + " · " + gs.getXp() + " XP";
        }
        return c.getRankingPoint() + " pts";
    }

    private static int displayLevel(Competitor c) {
        if (c.getUser() != null && c.getUser().getGameState() != null) {
            return c.getUser().getGameState().getLevel();
        }
        return Math.max(1, c.getRankingPoint() / 1_000_000);
    }

    private void buildLeaderboard() {
        if (leaderboard == null || boardList == null || myCard == null) {
            return;
        }
        List<Competitor> rawCompetitors = leaderboard.getCompetitors();

        if (rawCompetitors == null || rawCompetitors.isEmpty()) {
            myCard.getChildren().clear();
            boardList.getChildren().clear();
            Label emptyLabel = new Label("No one to show!");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            boardList.getChildren().add(emptyLabel);
            return;
        }

        List<Competitor> competitors = new ArrayList<>(rawCompetitors);
        competitors.sort(new Comparator<Competitor>() {
            @Override
            public int compare(Competitor c1, Competitor c2) {
                return Integer.compare(c2.getRankingPoint(), c1.getRankingPoint());
            }
        });

        int total = competitors.size();
        int myIndex = -1;
        if (currentUser != null) {
            for (int i = 0; i < competitors.size(); i++) {
                Competitor c = competitors.get(i);
                if (c.getUser() != null && c.getUser().getId() == currentUser.getId()) {
                    myIndex = i;
                    break;
                }
            }
        }

        boolean isUnranked = (myIndex == -1);
        int myRank = isUnranked ? -1 : myIndex + 1;
        Competitor me = isUnranked ? null : competitors.get(myIndex);

        myCard.getChildren().clear();
        myCard.setAlignment(Pos.CENTER);
        myCard.setPadding(new Insets(20));
        myCard.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");
        myCard.getStyleClass().addAll(
                Styles.BORDERED,
                Styles.ROUNDED,
                Styles.BG_ACCENT_SUBTLE,
                Styles.ELEVATED_1);

        VBox gapContainer = new VBox(5);
        gapContainer.setAlignment(Pos.CENTER);

        if (isUnranked) {
            Label topLabel = new Label("Play a match to get ranked!");
            topLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px; -fx-font-style: italic;");
            gapContainer.getChildren().add(topLabel);
        } else if (myIndex > 0) {
            Competitor playerAbove = competitors.get(myIndex - 1);
            int gap = playerAbove.getRankingPoint() - me.getRankingPoint();

            Label gapLabel = new Label(gap + " pts to reach #" + myIndex);
            gapLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px; -fx-font-style: italic;");

            ProgressBar gapBar = new ProgressBar();
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

        Label myTitleLabel = new Label("Your position:");
        myTitleLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Label myRankText = new Label(isUnranked ? "Unranked" : "#" + myRank + " of " + total);
        myRankText.getStyleClass().add(Styles.TITLE_3);
        myRankText.setAlignment(Pos.CENTER);

        HBox myStatsBox = new HBox(10);
        myStatsBox.setAlignment(Pos.CENTER);

        Label myXp = new Label(me != null ? formatProgression(me) : "Lv 1 · 0 XP");
        myXp.setStyle("-fx-font-weight: bold;");

        Label mySep = new Label("|");

        Label myWin = new Label((me != null ? String.format("%.0f", me.getWinRate()) : "0") + "% Win Rate");
        myWin.setStyle("-fx-font-weight: bold;");

        myStatsBox.getChildren().addAll(myXp, mySep, myWin);
        myCard.getChildren().addAll(myTitleLabel, myRankText, gapContainer, myStatsBox);

        boardList.getChildren().clear();

        for (int i = 0; i < competitors.size(); i++) {
            final Competitor c = competitors.get(i);
            final int index = i;

            HBox line = new HBox(15);
            line.setAlignment(Pos.CENTER_LEFT);
            line.setPadding(new Insets(10, 20, 10, 20));
            line.setMaxWidth(600);
            VBox.setMargin(line, new Insets(0, 0, 0, 0));

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

            int levelVal = displayLevel(c);
            Label levelBadge = new Label("Lvl " + levelVal);
            levelBadge.setStyle(
                    "-fx-background-color: #34495e; " +
                            "-fx-text-fill: #ecf0f1; " +
                            "-fx-padding: 2 8 2 8; " +
                            "-fx-background-radius: 10; " +
                            "-fx-font-size: 9px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-opacity: 0.8;");

            line.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    line.setStyle(finalBaseStyle + "-fx-background-color: #384351; -fx-cursor: hand;");
                    line.setScaleX(1.01);
                    line.setScaleY(1.01);
                }
            });

            line.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    line.setStyle(finalBaseStyle);
                    line.setScaleX(1.0);
                    line.setScaleY(1.0);
                }
            });

            line.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (onOpenProfile != null) {
                        onOpenProfile.accept(c, competitors);
                    }
                }
            });

            Label pos = new Label("#" + String.valueOf(i + 1));
            pos.getStyleClass().add(Styles.TEXT_BOLD);

            if (i == 0) {
                pos.setStyle("-fx-text-fill: #FFD700;");
            } else if (i == 1) {
                pos.setStyle("-fx-text-fill: #C0C0C0;");
            } else if (i == 2) {
                pos.setStyle("-fx-text-fill: #CD7F32;");
            }

            String nameText = c.getUser() != null
                    ? c.getUser().getDisplayName()
                    : "Player " + (i + 1);
            Label name = new Label(nameText);
            name.setStyle("-fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label xpLabel = new Label(formatProgression(c));
            xpLabel.setStyle("-fx-text-fill: #2775b1; -fx-font-weight: bold;");

            Label winLabel = new Label(String.format("%.0f", c.getWinRate()) + "% Win Rate");
            winLabel.setStyle("-fx-text-fill: #308f5a; -fx-font-weight: bold;");

            line.getChildren().addAll(pos, name, levelBadge, spacer, xpLabel, new Label("|"), winLabel);
            boardList.getChildren().add(line);
        }
    }
}

package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.DailyChallenge;
import com.codedu.models.Reward;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Controller
public class DailyChallengeController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox challengeList;

    private DailyChallenge todayChallenge;
    private Consumer<DailyChallenge> onStartChallenge;

    public void setTodayChallenge(DailyChallenge todayChallenge) {
        this.todayChallenge = todayChallenge;
        buildChallenges();
    }

    public void setOnStartChallenge(Consumer<DailyChallenge> onStartChallenge) {
        this.onStartChallenge = onStartChallenge;
        buildChallenges();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        buildChallenges();
    }

    private void buildChallenges() {
        if (challengeList == null) {
            return;
        }
        challengeList.getChildren().clear();

        List<DailyChallenge> challenges = new ArrayList<>();
        if (todayChallenge != null) {
            challenges.add(todayChallenge);
        }

        if (challenges.isEmpty()) {
            challenges.add(DailyChallenge.builder()
                    .name("Loops & counters")
                    .description("Write a function that prints the numbers from 1 to 100 and counts how many are even.")
                    .reward(Reward.builder()
                            .token(25)
                            .xp(50)
                            .build())
                    .build());
        }

        // Additional sample challenges for visual richness
        challenges.add(DailyChallenge.builder()
                .name("Array practice")
                .description("Work with arrays: sum numbers, find max, and reverse the list.")
                .reward(Reward.builder()
                        .token(15)
                        .xp(40)
                        .build())
                .build());

        challenges.add(DailyChallenge.builder()
                .name("Debug the loop")
                .description("Fix an off-by-one error in a for-loop and make tests pass.")
                .reward(Reward.builder()
                        .token(10)
                        .xp(35)
                        .build())
                .build());

        for (DailyChallenge ch : challenges) {
            VBox card = new VBox(6);
            card.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            card.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            Label chTitle = new Label(ch.getName());
            chTitle.getStyleClass().add(Styles.TEXT_BOLD);

            Label chBody = new Label(ch.getDescription());
            chBody.setWrapText(true);

            int xp = 0;
            int token = 0;
            if (ch.getReward() != null) {
                xp = ch.getReward().getXp();
                token = ch.getReward().getToken();
            }

            Label chMeta = new Label(xp + " XP · " + token + " tokens");
            chMeta.getStyleClass().add(Styles.TEXT_SUBTLE);

            card.getChildren().addAll(chTitle, chBody, chMeta);

            if (onStartChallenge != null) {
                card.getStyleClass().add(Styles.INTERACTIVE);
                card.setOnMouseClicked(e -> onStartChallenge.accept(ch));
            }

            challengeList.getChildren().add(card);
        }
    }
}


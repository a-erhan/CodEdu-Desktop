package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.DailyChallenge;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DailyChallengeController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox challengeList;

    private DailyChallenge todayChallenge;

    public void setTodayChallenge(DailyChallenge todayChallenge) {
        this.todayChallenge = todayChallenge;
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
                    .xpRewards(50)
                    .tokenRewards(25)
                    .build());
        }

        // Additional sample challenges for visual richness
        challenges.add(DailyChallenge.builder()
                .name("Array practice")
                .description("Work with arrays: sum numbers, find max, and reverse the list.")
                .xpRewards(40)
                .tokenRewards(15)
                .build());
        challenges.add(DailyChallenge.builder()
                .name("Debug the loop")
                .description("Fix an off-by-one error in a for-loop and make tests pass.")
                .xpRewards(35)
                .tokenRewards(10)
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

            Label chMeta = new Label(ch.getXpRewards() + " XP · " + ch.getTokenRewards() + " tokens");
            chMeta.getStyleClass().add(Styles.TEXT_SUBTLE);

            card.getChildren().addAll(chTitle, chBody, chMeta);
            challengeList.getChildren().add(card);
        }
    }
}


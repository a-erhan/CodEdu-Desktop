package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.DailyChallenge;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

@Controller
public class ChallengeController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label metaLabel;
    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox challengeCard;
    @FXML
    private Label problemTitle;
    @FXML
    private Label problemDescription;

    @FXML
    private VBox editorCard;
    @FXML
    private Label editorLabel;
    @FXML
    private TextArea codeArea;
    @FXML
    private Button runButton;
    @FXML
    private Button submitButton;
    @FXML
    private Label outputLabel;
    @FXML
    private TextArea outputArea;

    private DailyChallenge challenge;

    public void setChallenge(DailyChallenge challenge) {
        this.challenge = challenge;
        applyChallenge();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (challengeCard != null) {
            challengeCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            challengeCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (editorCard != null) {
            editorCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            editorCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (editorLabel != null) {
            editorLabel.getStyleClass().add(Styles.TEXT_BOLD);
        }
        if (runButton != null) {
            runButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
        }
        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
        }
        if (outputLabel != null) {
            outputLabel.getStyleClass().add(Styles.TEXT_BOLD);
        }

        applyChallenge();
    }

    private void applyChallenge() {
        if (challenge == null || problemTitle == null || problemDescription == null) {
            return;
        }
        problemTitle.setText(challenge.getName());
        problemDescription.setText(challenge.getDescription());
        if (metaLabel != null) {
            metaLabel.setText(challenge.getXpRewards() + " XP · " + challenge.getTokenRewards() + " tokens");
            metaLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        }
    }
}


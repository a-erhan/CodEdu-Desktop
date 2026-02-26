package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

@Controller
public class MatchmakingController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox statusCard;
    @FXML
    private Label statusLabel;
    @FXML
    private Label statusValue;

    @FXML
    private VBox challengeCard;
    @FXML
    private Label challengeHeader;
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
    private HBox actionRow;
    @FXML
    private Button runButton;
    @FXML
    private Button submitButton;
    @FXML
    private Label outputLabel;
    @FXML
    private TextArea outputArea;

    @FXML
    private VBox scoreCard;
    @FXML
    private Label scoreTitle;
    @FXML
    private Label youScore;
    @FXML
    private Label opponentScore;
    @FXML
    private Label attemptsTitle;
    @FXML
    private Label youAttempts;
    @FXML
    private Label opponentAttempts;

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }

        if (statusCard != null) {
            statusCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            statusCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (statusLabel != null) {
            statusLabel.getStyleClass().add(Styles.TEXT_BOLD);
        }
        if (statusValue != null) {
            statusValue.getStyleClass().add(Styles.TEXT_SUBTLE);
        }

        if (challengeCard != null) {
            challengeCard.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));
            challengeCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (challengeHeader != null) {
            challengeHeader.getStyleClass().add(Styles.TEXT_BOLD);
        }
        if (problemTitle != null) {
            problemTitle.getStyleClass().add(Styles.TEXT_SUBTLE);
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

        if (scoreCard != null) {
            scoreCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            scoreCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (scoreTitle != null) {
            scoreTitle.getStyleClass().add(Styles.TEXT_BOLD);
        }
        if (youScore != null) {
            youScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        }
        if (opponentScore != null) {
            opponentScore.getStyleClass().add(Styles.TEXT_SUBTLE);
        }
        if (attemptsTitle != null) {
            attemptsTitle.getStyleClass().add(Styles.TEXT_BOLD);
        }
        if (youAttempts != null) {
            youAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);
        }
        if (opponentAttempts != null) {
            opponentAttempts.getStyleClass().add(Styles.TEXT_SUBTLE);
        }
    }
}


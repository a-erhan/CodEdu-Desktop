package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.DailyChallenge;
import com.codedu.models.learning.Question;
import com.codedu.services.interfaces.DailyChallengeService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

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
    private final DailyChallengeService dailyChallengeService;
    private DailyChallenge todayChallenge;
    private Runnable onBack;
    private Consumer<Question> onStartQuestion;

    public DailyChallengeController(DailyChallengeService dailyChallengeService) {
        this.dailyChallengeService = dailyChallengeService;
    }

    public void setOnStartQuestion(Consumer<Question> onStartQuestion) {
        this.onStartQuestion = onStartQuestion;
        buildChallenges();
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_1);
            titleLabel.setStyle("-fx-text-fill: white;");
        }
        if (subtitleLabel != null) {
            subtitleLabel.setStyle("-fx-text-fill: #A0AAB4;");
        }
        if (challengeList != null) {
            challengeList.setAlignment(Pos.TOP_CENTER);
        }
        try {
            this.todayChallenge = dailyChallengeService.getTodaysChallengeEntity();
            buildChallenges();
        } catch (Exception e) {
            System.err.println("Günün görevi yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    private void buildChallenges() {
        if (challengeList == null) {
            return;
        }
        challengeList.getChildren().clear();

        if (todayChallenge == null) {
            if (titleLabel != null) titleLabel.setText("No Challenge Today");
            if (subtitleLabel != null) subtitleLabel.setText("Check back later!");
            return;
        }

        if (titleLabel != null) titleLabel.setText(todayChallenge.getName());
        if (subtitleLabel != null) subtitleLabel.setText(todayChallenge.getDescription());

        // --- Theme Colors ---
        final String main_blue = "#00ADEF";
        final String main_dark = "#303846";
        final String alt_dark = "#2A313C";
        final String muted_orange = "#D9822B"; // The "Not bright" orange

        List<Question> questions = todayChallenge.getQuestions();
        int totalXp = todayChallenge.getReward() != null ? todayChallenge.getReward().getXp() : 0;
        int xpPerQuestion = questions.isEmpty() ? 0 : totalXp / questions.size();

        // 1. Header Summary Card
        VBox summaryBox = new VBox(10);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setMaxWidth(850);
        summaryBox.setStyle("-fx-background-color: " + alt_dark + "; -fx-border-color: " + main_blue + "; -fx-border-width: 0 0 0 4; -fx-background-radius: 10;");

        Label rewardSummary = new Label("DAILY REWARD: " + totalXp + " XP");
        rewardSummary.setStyle("-fx-text-fill: " + muted_orange + "; -fx-font-weight: bold; -fx-font-size: 18px;");
        Label rewardDetail = new Label("Tackle these tasks to sharpen your skills and earn rewards.");
        rewardDetail.setStyle("-fx-text-fill: white;");
        summaryBox.getChildren().addAll(rewardSummary, rewardDetail);

        challengeList.getChildren().add(summaryBox);

        // 2. Main Layout (Tasks + Sidebar)
        HBox mainContent = new HBox(30);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(20, 0, 0, 0));

        VBox taskContainer = new VBox(15);
        taskContainer.setPrefWidth(550);

        int questionNumber = 1;
        for (final Question q : questions) {
            final int currentNum = questionNumber;
            final HBox card = new HBox(15);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(18, 25, 18, 25));

            final String baseStyle = "-fx-background-color: " + (currentNum % 2 == 0 ? alt_dark : main_dark) + "; " +
                    "-fx-border-color: #404856; -fx-border-width: 1px; " +
                    "-fx-background-radius: 20; -fx-border-radius: 20;";
            card.setStyle(baseStyle);

            VBox textContainer = new VBox(5);
            textContainer.setAlignment(Pos.CENTER_LEFT);

            // Muted Orange Task Label
            final Label chTitle = new Label("Task " + currentNum);
            chTitle.getStyleClass().add(Styles.TEXT_BOLD);
            chTitle.setStyle("-fx-text-fill: " + muted_orange + "; -fx-font-size: 16px;");

            final Label typeBadge = new Label(String.valueOf(q.getQuestionType()).toUpperCase());
            typeBadge.setStyle("-fx-background-color: " + main_blue + "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 10px;");

            HBox titleRow = new HBox(12, chTitle, typeBadge);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            final Label chBody = new Label("Difficulty: " + q.getQuestionDifficulty());
            chBody.setStyle("-fx-text-fill: #A0AAB4; -fx-font-size: 12px;");

            textContainer.getChildren().addAll(titleRow, chBody);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            final Label xpLabel = new Label("+" + xpPerQuestion + " XP");
            xpLabel.setStyle("-fx-text-fill: " + main_blue + "; -fx-font-weight: bold; -fx-font-size: 15px;");

            card.getChildren().addAll(textContainer, spacer, xpLabel);

            if (onStartQuestion != null) {
                card.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        card.setStyle("-fx-background-color: " + main_blue + "; -fx-border-color: white; -fx-background-radius: 20; -fx-border-radius: 20;");
                        card.setCursor(Cursor.HAND);
                        card.setScaleX(1.03); card.setScaleY(1.03);
                        // Shift text to white on hover for better contrast
                        chTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                        chBody.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                        xpLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
                    }
                });
                card.setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        card.setStyle(baseStyle);
                        card.setScaleX(1.0); card.setScaleY(1.0);
                        // Revert to original colors
                        chTitle.setStyle("-fx-text-fill: " + muted_orange + "; -fx-font-size: 16px;");
                        chBody.setStyle("-fx-text-fill: #A0AAB4; -fx-font-size: 12px;");
                        xpLabel.setStyle("-fx-text-fill: " + main_blue + "; -fx-font-weight: bold; -fx-font-size: 15px;");
                    }
                });
                card.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) { onStartQuestion.accept(q); }
                });
            }
            taskContainer.getChildren().add(card);
            questionNumber++;
        }

        // Sidebar
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(270);

        VBox mascotCard = new VBox(15);
        mascotCard.setAlignment(Pos.CENTER);
        mascotCard.setPadding(new Insets(25));
        mascotCard.setStyle("-fx-background-color: " + main_dark + "; -fx-background-radius: 25;");

        Label mascotIcon = new Label("🛡️");
        mascotIcon.setStyle("-fx-font-size: 50px;");
        Label mascotText = new Label("New challenges await!");
        mascotText.setStyle("-fx-text-fill: white; -fx-font-style: italic; -fx-text-alignment: center;");
        mascotCard.getChildren().addAll(mascotIcon, mascotText);

        VBox statsCard = new VBox(10);
        statsCard.setPadding(new Insets(20));
        statsCard.setStyle("-fx-border-color: " + main_blue + "; -fx-border-radius: 20; -fx-border-width: 1px;");
        Label statsTitle = new Label("DAILY STATUS");
        statsTitle.setStyle("-fx-text-fill: " + main_blue + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        statsCard.getChildren().addAll(statsTitle, new Separator(), new Label("Active streak: 1 day"));

        sidebar.getChildren().addAll(mascotCard, statsCard);
        mainContent.getChildren().addAll(taskContainer, sidebar);
        challengeList.getChildren().add(mainContent);
    }
}
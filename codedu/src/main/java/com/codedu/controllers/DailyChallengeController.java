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

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private VBox challengeList;

    private final DailyChallengeService dailyChallengeService;
    private DailyChallenge todayChallenge;
    private Runnable onBack;
    private Consumer<Question> onStartQuestion;

    private final String LOGO_BLUE = "#00AEEF";
    private final String LOGO_ORANGE = "#F7941D";
    private final String DARK_BG = "#2e3440";
    private final String CARD_BG = "#3b4252";
    private final String BORDER_COLOR = "#4c566a";

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
            titleLabel.getStyleClass().add(Styles.TITLE_3);
            titleLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold;");
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
        if (challengeList == null) return;
        challengeList.getChildren().clear();

        if (todayChallenge == null) {
            if (titleLabel != null) titleLabel.setText("No Challenge Today");
            if (subtitleLabel != null) subtitleLabel.setText("Check back later!");
            return;
        }

        if (titleLabel != null) titleLabel.setText(todayChallenge.getName().toUpperCase());
        if (subtitleLabel != null) subtitleLabel.setText(todayChallenge.getDescription());

        List<Question> questions = todayChallenge.getQuestions();
        int totalXp = todayChallenge.getReward() != null ? todayChallenge.getReward().getXp() : 0;
        int xpPerQuestion = questions.isEmpty() ? 0 : totalXp / questions.size();

        // summarycard
        VBox summaryBox = new VBox(10);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setMaxWidth(850);
        summaryBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + LOGO_BLUE + "; -fx-border-width: 0 0 0 5; -fx-background-radius: 12;");

        Label rewardSummary = new Label("DAILY REWARDS AVAILABLE: " + totalXp + " XP");
        rewardSummary.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        Label rewardDetail = new Label("Complete today's tasks to claim your progress rewards.");
        rewardDetail.setStyle("-fx-text-fill: #A0AAB4;");
        summaryBox.getChildren().addAll(rewardSummary, rewardDetail);

        challengeList.getChildren().add(summaryBox);

        // content
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

            final String baseStyle = "-fx-background-color: " + CARD_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1px; -fx-background-radius: 12; -fx-border-radius: 12;";
            card.setStyle(baseStyle);

            VBox textContainer = new VBox(5);
            textContainer.setAlignment(Pos.CENTER_LEFT);

            final Label chTitle = new Label("TASK 0" + currentNum);
            chTitle.getStyleClass().add(Styles.TEXT_BOLD);
            chTitle.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-size: 14px;");

            final Label typeBadge = new Label(String.valueOf(q.getQuestionType()).toUpperCase());
            typeBadge.setStyle("-fx-background-color: " + LOGO_BLUE + "; -fx-text-fill: " + DARK_BG + "; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;");

            HBox titleRow = new HBox(12, chTitle, typeBadge);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            final Label chBody = new Label(q.getTitle());
            chBody.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

            textContainer.getChildren().addAll(titleRow, chBody);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            final Label xpLabel = new Label("+" + xpPerQuestion + " XP");
            xpLabel.setStyle("-fx-text-fill: " + LOGO_BLUE + "; -fx-font-weight: bold; -fx-font-size: 15px;");

            card.getChildren().addAll(textContainer, spacer, xpLabel);

            if (onStartQuestion != null) {
                card.setOnMouseEntered(e -> {
                    card.setStyle("-fx-background-color: #434c5e; -fx-border-color: " + LOGO_BLUE + "; -fx-background-radius: 12; -fx-border-radius: 12;");
                    card.setCursor(Cursor.HAND);
                    card.setScaleX(1.02); card.setScaleY(1.02);
                });
                card.setOnMouseExited(e -> {
                    card.setStyle(baseStyle);
                    card.setScaleX(1.0); card.setScaleY(1.0);
                });
                card.setOnMouseClicked(e -> onStartQuestion.accept(q));
            }
            taskContainer.getChildren().add(card);
            questionNumber++;
        }

        // sidebar
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(270);

        VBox mascotCard = new VBox(15);
        mascotCard.setAlignment(Pos.CENTER);
        mascotCard.setPadding(new Insets(25));
        mascotCard.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #3b4252, " + LOGO_BLUE + "44);" +
                        "-fx-background-radius: 25;" +
                        "-fx-border-color: " + LOGO_BLUE + "88;" +
                        "-fx-border-radius: 25;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(three-pass-box, " + LOGO_BLUE + "22, 10, 0, 0, 0);"
        );

        Label mascotIcon = new Label("🛡️");
        mascotIcon.setStyle("-fx-font-size: 50px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 2);");
        Label mascotText = new Label("Challenge\nyourself!");
        mascotText.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-alignment: center;" +
                        "-fx-line-spacing: 2;"
        );
        mascotText.setStyle("-fx-text-fill: #eceff4; -fx-font-style: italic; -fx-text-alignment: center; -fx-font-size: 12px;");
        mascotCard.getChildren().addAll(mascotIcon, mascotText);

        sidebar.getChildren().add(mascotCard);
        mainContent.getChildren().addAll(taskContainer, sidebar);
        challengeList.getChildren().add(mainContent);
    }
}
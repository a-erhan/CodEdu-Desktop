package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.DailyChallenge;
import com.codedu.models.learning.Question;
import com.codedu.services.interfaces.DailyChallengeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    // Callback to return to previous view
    private Runnable onBack;

    // 2. Artık karta tıklayınca tüm görevi değil, tıklanan o özel "Soruyu
    // (Question)" başlatacağız
    private Consumer<Question> onStartQuestion;

    // Spring Boot Dependency Injection (Constructor Injection)
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
        }
        try {
            this.todayChallenge = dailyChallengeService.getTodaysChallenge();
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
            if (titleLabel != null)
                titleLabel.setText("No Challenge Today");
            if (subtitleLabel != null)
                subtitleLabel.setText("Check back later or explore other sections!");

            Label emptyLabel = new Label("We couldn't find any questions for today's challenge.");
            emptyLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
            challengeList.getChildren().add(emptyLabel);
            return;
        }

        if (titleLabel != null)
            titleLabel.setText(todayChallenge.getName());
        if (subtitleLabel != null)
            subtitleLabel.setText(todayChallenge.getDescription());

        List<Question> questions = todayChallenge.getQuestions();

        int totalXp = todayChallenge.getReward() != null ? todayChallenge.getReward().getXp() : 0;
        int xpPerQuestion = questions.isEmpty() ? 0 : totalXp / questions.size();

        int questionNumber = 1;

        for (Question q : questions) {
            VBox card = new VBox(6);
            card.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            card.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            Label chTitle = new Label("Task " + questionNumber + " - " + q.getQuestionType());
            chTitle.getStyleClass().add(Styles.TEXT_BOLD);

            Label chBody = new Label("Difficulty: " + q.getQuestionDifficulty() + " level algorithm challenge.");
            chBody.setWrapText(true);

            Label chMeta = new Label(xpPerQuestion + " XP");
            chMeta.getStyleClass().add(Styles.TEXT_SUBTLE);

            card.getChildren().addAll(chTitle, chBody, chMeta);

            if (onStartQuestion != null) {
                card.getStyleClass().add(Styles.INTERACTIVE);
                card.setOnMouseClicked(e -> onStartQuestion.accept(q));
            }

            challengeList.getChildren().add(card);
            questionNumber++;
        }
    }
}
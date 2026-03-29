package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.Question;
import com.codedu.services.QuestionEvaluationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class QuestionSolverController {

    @FXML
    private Label questionTitleLabel;
    @FXML
    private Label questionDescriptionLabel;
    @FXML
    private TextArea codeEditorArea;
    @FXML
    private Label resultMessageLabel;
    @FXML
    private Button submitButton;

    @Autowired
    private QuestionEvaluationService questionEvaluationService;

    private Question currentQuestion;

    @FXML
    public void initialize() {
        if (questionTitleLabel != null) {
            questionTitleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.DENSE);
        }
    }

    public void setQuestion(Question question) {
        this.currentQuestion = question;
        if (questionTitleLabel != null) {
            questionTitleLabel.setText(question.getTitle());
        }
        if (questionDescriptionLabel != null) {
            questionDescriptionLabel.setText(question.getContent());
        }
        if (resultMessageLabel != null) {
            resultMessageLabel.setText("");
        }
        if (codeEditorArea != null) {
            if (question instanceof CodeImplementationQuestion codeQuestion) {
                codeEditorArea.setText(codeQuestion.getBoilerplateCode());
            } else {
                codeEditorArea.clear();
            }
        }
    }

    @FXML
    public void onSubmitCode() {
        if (currentQuestion == null)
            return;

        String userCode = codeEditorArea.getText();
        if (userCode == null || userCode.trim().isEmpty()) {
            showResult("Please write some code before submitting.", false);
            return;
        }

        submitButton.setDisable(true);
        showResult("Evaluating... Please wait. This can take up to 10 seconds.", true);
        resultMessageLabel.getStyleClass().remove(Styles.SUCCESS);

        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return questionEvaluationService.evaluate(currentQuestion, userCode);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }).thenAccept((isCorrect) -> {
            javafx.application.Platform.runLater(() -> {
                submitButton.setDisable(false);
                if (isCorrect) {
                    showResult("Correct! Well done! All test cases passed.", true);
                    // Optionally notify parent callback
                } else {
                    showResult("Incorrect solution or compilation failed. Please try again.", false);
                }
            });
        });
    }

    private void showResult(String message, boolean isSuccess) {
        if (resultMessageLabel != null) {
            resultMessageLabel.setText(message);
            resultMessageLabel.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER);
            if (isSuccess) {
                resultMessageLabel.getStyleClass().add(Styles.SUCCESS);
            } else {
                resultMessageLabel.getStyleClass().add(Styles.DANGER);
            }
        }
    }
}

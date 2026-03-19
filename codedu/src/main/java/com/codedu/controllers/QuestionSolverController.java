package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Question;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
            codeEditorArea.clear();
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

        // Basic verification against the expected solution
        String expectedSolution = currentQuestion.getSolution();

        // Very basic simple match
        if (expectedSolution != null && userCode.trim().equals(expectedSolution.trim())) {
            showResult("Correct! Well done.", true);
            // Optionally, we could notify a parent callback or unlock next step here
        } else {
            showResult("Incorrect solution. Please try again.", false);
        }
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

package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.Question;
import com.codedu.services.interfaces.QuestionEvaluationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import java.util.function.Consumer;

@Controller
@Scope("prototype")
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
    @FXML
    private Button giveUpButton;

    private java.util.function.Supplier<Boolean> heartCheckCallback;
    private Runnable onWrongAnswerCallback;

    @Autowired
    private QuestionEvaluationService questionEvaluationService;

    private Question currentQuestion;

    private Consumer<Boolean> onSuccessCallback;

    @FXML
    public void initialize() {
        if (questionTitleLabel != null) {
            questionTitleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (submitButton != null) {
            submitButton.getStyleClass().addAll(Styles.ACCENT, Styles.DENSE);
        }

        // 🚀 Create the button programmatically if FXML didn't provide one
        if (giveUpButton != null) {
            giveUpButton.setText("Give Up");
            // Use Styles.DANGER or Styles.WARNING to make it stand out
            giveUpButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.WARNING, Styles.DENSE);

            // Hide it until a mistake is made
            giveUpButton.setVisible(false);
            giveUpButton.setManaged(false);
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

    public void setOnSuccessCallback(Consumer<Boolean> callback) {
        this.onSuccessCallback = callback;
    }

    public void setLocked(boolean isLocked) {
        if (codeEditorArea != null) {
            codeEditorArea.setEditable(!isLocked); // Disable typing
        }
        if (submitButton != null) {
            submitButton.setVisible(!isLocked); // Hide the run button completely
        }
        if (isLocked) {
            showResult("✅ Completed", true);
        }
    }

    @FXML
    public void onSubmitCode() {
        if (heartCheckCallback != null && !heartCheckCallback.get()) {
            return;
        }

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
                    setLocked(true);
                    if (onSuccessCallback != null) {
                        onSuccessCallback.accept(true);
                    }
                } else {
                    showResult("Incorrect solution or compilation failed. Please try again.", false);
                    if (onWrongAnswerCallback != null) onWrongAnswerCallback.run();
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

    public void setHeartCheckCallback(java.util.function.Supplier<Boolean> callback) {
        this.heartCheckCallback = callback;
    }
    public void setOnWrongAnswerCallback(Runnable callback) {
        this.onWrongAnswerCallback = callback;
    }

    public void showSolutionState(String solution) {
        if (codeEditorArea != null) {
            codeEditorArea.setText(solution);
            codeEditorArea.setEditable(false);

            codeEditorArea.setStyle("-fx-control-inner-background: #1e272e; -fx-text-fill: #2ecc71; -fx-font-family: 'Consolas';");
        }
        if (submitButton != null) {
            submitButton.setVisible(false);
            submitButton.setManaged(false);
        }
        showResult("💡 Solution Revealed", true);
    }

    public void setupGiveUpLogic(String solution, Runnable onGiveUpAction) {
        if (giveUpButton == null) return;

        // Reset state just in case this controller is reused
        giveUpButton.setVisible(false);
        giveUpButton.setManaged(false);

        giveUpButton.setOnAction(e -> {
            // Swap text and lock editor
            codeEditorArea.setText(solution);
            codeEditorArea.setEditable(false);
            codeEditorArea.setStyle("-fx-control-inner-background: #1e272e; -fx-text-fill: #2ecc71; -fx-font-family: 'Consolas';");

            // Hide buttons after use
            submitButton.setVisible(false);
            submitButton.setManaged(false);
            giveUpButton.setVisible(false);
            giveUpButton.setManaged(false);

            showResult("💡 Solution Revealed", true);
            if (onGiveUpAction != null) onGiveUpAction.run();
        });
    }

    public void showGiveUpButton() {
        if (giveUpButton != null) {
            // 🚀 1. Make it physically visible
            giveUpButton.setVisible(true);

            // 🚀 2. Tell the layout to give it space again
            giveUpButton.setManaged(true);

            // 🚀 3. Force the parent container to recalculate its size
            if (giveUpButton.getParent() != null) {
                giveUpButton.getParent().requestLayout();
            }

            System.out.println("DEBUG: Give Up button is now visible and managed.");
        } else {
            System.out.println("DEBUG: Give Up button is NULL!");
        }
    }
}

package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.services.AIChatbotService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class AIChatbotController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox chatCard;
    @FXML
    private VBox chatList;
    @FXML
    private VBox card;
    @FXML
    private Label promptLabel;
    @FXML
    private TextArea questionArea;
    @FXML
    private TextField tagsField;
    @FXML
    private Label requestsLabel;
    @FXML
    private Button askButton;
    @FXML
    private Label noteLabel;
    @FXML
    private VBox codeCard;
    @FXML
    private TextArea codeArea;

    private int remainingRequests = 3;

    private final AIChatbotService geminiAiService;

    @Autowired
    public AIChatbotController(AIChatbotService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    public void setRemainingRequests(int remainingRequests) {
        this.remainingRequests = remainingRequests;
        updateRequestsLabel();
        updateAskButtonState();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null)
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        if (chatCard != null) {
            chatCard.setPadding(new Insets(12, 14, 12, 14));
            chatCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (card != null) {
            card.setPadding(new Insets(16, 18, 16, 18));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (codeCard != null) {
            codeCard.setPadding(new Insets(16, 18, 16, 18));
            codeCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (promptLabel != null)
            promptLabel.getStyleClass().add(Styles.TEXT_BOLD);
        if (noteLabel != null)
            noteLabel.getStyleClass().add(Styles.TEXT_SUBTLE);

        if (askButton != null) {
            askButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            askButton.setOnAction(e -> handleAsk());
        }

        if (questionArea != null && askButton != null) {
            questionArea.textProperty().addListener((obs, o, n) -> updateAskButtonState());
        }

        updateRequestsLabel();
        updateAskButtonState();
    }

    private void updateRequestsLabel() {
        if (requestsLabel != null) {
            requestsLabel.setText(remainingRequests + " AI requests left");
        }
    }

    private void updateAskButtonState() {
        if (askButton == null || questionArea == null)
            return;
        boolean hasQuestion = !questionArea.getText().trim().isEmpty();
        askButton.setDisable(!hasQuestion || remainingRequests <= 0);
    }

    private void handleAsk() {
        if (remainingRequests <= 0 || chatList == null || questionArea == null) {
            updateAskButtonState();
            return;
        }

        String question = questionArea.getText().trim();
        String tags = tagsField != null ? tagsField.getText().trim() : "";
        String code = codeArea != null ? codeArea.getText().trim() : "";

        if (question.isEmpty()) {
            updateAskButtonState();
            return;
        }

        askButton.setDisable(true);

        VBox userBubble = new VBox(2);
        userBubble.setAlignment(Pos.TOP_LEFT);

        Label who = new Label("You");
        who.getStyleClass().add(Styles.TEXT_BOLD);

        StringBuilder userDisplay = new StringBuilder(question);
        if (!tags.isEmpty())
            userDisplay.append("\n\nTags: ").append(tags);
        if (!code.isEmpty())
            userDisplay.append("\n\n[Code snippet attached]");

        Label body = new Label(userDisplay.toString());
        body.setWrapText(true);
        userBubble.getChildren().addAll(who, body);
        chatList.getChildren().add(userBubble);

        VBox aiBubble = new VBox(2);
        aiBubble.setAlignment(Pos.TOP_LEFT);
        aiBubble.setPadding(new Insets(6, 0, 0, 0));

        Label aiWho = new Label("AI Tutor");
        aiWho.getStyleClass().add(Styles.TEXT_BOLD);

        Label aiBody = new Label("Thinking...");
        aiBody.setWrapText(true);
        aiBody.getStyleClass().add(Styles.TEXT_SUBTLE);
        aiBubble.getChildren().addAll(aiWho, aiBody);
        chatList.getChildren().add(aiBubble);

        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append("User Question: ").append(question).append("\n\n");
        if (!tags.isEmpty())
            fullPrompt.append("Relevant Topics/Tags: ").append(tags).append("\n\n");
        if (!code.isEmpty())
            fullPrompt.append("Provided Code:\n```java\n").append(code).append("\n```\n");
        fullPrompt.append("\nPlease provide a helpful, educational response.");

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() {
                return geminiAiService.askAi(fullPrompt.toString());
            }
        };

        aiTask.setOnSucceeded(event -> {
            String responseTxt = aiTask.getValue();
            aiBody.setText(responseTxt);

            String codeBlock = extractCode(responseTxt);
            if (codeBlock != null && codeArea != null) {
                codeArea.setText(codeBlock);
            }

            remainingRequests = Math.max(0, remainingRequests - 1);
            updateRequestsLabel();

            questionArea.clear();
            if (tagsField != null)
                tagsField.clear();

            updateAskButtonState();
        });

        aiTask.setOnFailed(event -> {
            aiBody.setText("Connection Error: Could not reach the AI Tutor.");
            updateAskButtonState();
        });

        new Thread(aiTask).start();
    }

    private String extractCode(String response) {
        if (response == null)
            return null;
        Pattern pattern = Pattern.compile("```(?:java)?\\n?(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
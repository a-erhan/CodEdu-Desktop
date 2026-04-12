package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.AIChatbotService;
import com.codedu.services.interfaces.InventoryItemService;
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

    private int remainingRequests = 0;
    private User currentUser;
    private final AIChatbotService geminiAiService;

    private final String LOGO_BLUE = "#00AEEF";
    private final String LOGO_ORANGE = "#F7941D";
    private final String DARK_BG = "#2e3440";
    private final String CARD_BG = "#3b4252";
    private final String BORDER_COLOR = "#4c566a";

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    public AIChatbotController(AIChatbotService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.remainingRequests = inventoryItemService.getAiRequestBalance(user);
        updateRequestsLabel();
        updateAskButtonState();
    }

    public void setRemainingRequests(int remainingRequests) {
        this.remainingRequests = remainingRequests;
        updateRequestsLabel();
        updateAskButtonState();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
            titleLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold;");
        }

        String commonCardStyle = "-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: "
                + BORDER_COLOR + "; -fx-border-radius: 12; -fx-border-width: 1;";

        if (chatCard != null) {
            chatCard.setPadding(new Insets(15));
            chatCard.setStyle(commonCardStyle);
        }
        if (card != null) {
            card.setPadding(new Insets(20));
            card.setStyle(commonCardStyle);
        }
        if (codeCard != null) {
            codeCard.setPadding(new Insets(15));
            codeCard.setStyle(commonCardStyle + "-fx-background-color: " + DARK_BG + ";");
        }

        if (askButton != null) {
            askButton.setStyle("-fx-background-color: " + LOGO_BLUE
                    + "; -fx-text-fill: #2e3440; -fx-font-weight: bold; -fx-background-radius: 20;");
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
            requestsLabel.setStyle("-fx-text-fill: " + (remainingRequests > 0 ? LOGO_BLUE : "#bf616a") + ";");
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

        VBox aiBubble = new VBox(2);
        aiBubble.setAlignment(Pos.TOP_LEFT);
        aiBubble.setPadding(new Insets(10));
        aiBubble.setStyle("-fx-background-color: #4c566a; -fx-background-radius: 10;");

        Label aiWho = new Label("AI Tutor");
        aiWho.setStyle("-fx-font-weight: bold; -fx-text-fill: " + LOGO_ORANGE + ";");

        Label aiBody = new Label("Thinking...");
        aiBody.setWrapText(true);
        aiBody.setStyle("-fx-text-fill: #eceff4;");
        aiBubble.getChildren().addAll(aiWho, aiBody);
        chatList.getChildren().add(aiBubble);

        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append("User Question: ").append(question).append("\n\n");
        if (!tags.isEmpty())
            fullPrompt.append("Tags: ").append(tags).append("\n\n");
        if (!code.isEmpty())
            fullPrompt.append("Code:\n").append(code);

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() {
                return geminiAiService.askAi(fullPrompt.toString());
            }
        };

        aiTask.setOnSucceeded(event -> {
            String aiResponse = aiTask.getValue();
            String extractedCode = extractCode(aiResponse);

            if (extractedCode != null) {
                codeArea.setText(extractedCode);
                String textOnly = aiResponse.replaceAll("(?s)```(?:java)?\\n?(.*?)```", "").trim();
                aiBody.setText(textOnly.isEmpty() ? "Here is the code snippet:" : textOnly);
            } else {
                aiBody.setText(aiResponse);
            }

            if (currentUser != null) {
                inventoryItemService.consumeAiRequest(currentUser);
                remainingRequests = inventoryItemService.getAiRequestBalance(currentUser);
            }
            updateRequestsLabel();
            questionArea.clear();
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
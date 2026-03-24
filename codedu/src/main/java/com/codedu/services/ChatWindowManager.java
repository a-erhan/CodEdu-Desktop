package com.codedu.services;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.ChatMessageDTO;
import com.codedu.models.user.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ChatWindowManager {

    private final WebSocketClientService webSocketClientService;
    private VBox activeChatBox;
    private String activeChatFriendId;
    private User currentUser;

    public ChatWindowManager(WebSocketClientService webSocketClientService) {
        this.webSocketClientService = webSocketClientService;
    }

    // ========== Connection ==========

    public void connectUser(User user) {
        this.currentUser = user;
        if (user != null) {
            webSocketClientService.connect(String.valueOf(user.getId()), this::handleIncomingMessage);
        }
    }

    // ========== Chat Window UI ==========

    public void openChatWindow(User friend) {
        if (this.currentUser == null) return;

        Stage chatStage = new Stage();
        chatStage.setTitle("Chat: " + friend.getUsername());

        VBox messageContainer = new VBox(10);
        messageContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        TextField inputField = new TextField();
        inputField.setPromptText("Type a message...");

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add(Styles.ACCENT);

        this.activeChatBox = messageContainer;
        this.activeChatFriendId = String.valueOf(friend.getId());

        sendButton.setOnAction(e -> {
            String text = inputField.getText();
            if (!text.trim().isEmpty()) {

                ChatMessageDTO msg = ChatMessageDTO.builder()
                        .senderId(String.valueOf(currentUser.getId()))
                        .receiverId(String.valueOf(friend.getId()))
                        .content(text)
                        .timestamp(System.currentTimeMillis())
                        .build();

                webSocketClientService.sendMessage(msg);

                Label selfLabel = new Label(text);
                selfLabel.setStyle("-fx-background-color: -color-accent-subtle; -fx-padding: 8px; -fx-background-radius: 8px;");
                HBox selfRow = new HBox(selfLabel);
                selfRow.setAlignment(Pos.CENTER_RIGHT);
                messageContainer.getChildren().add(selfRow);

                inputField.clear();
            }
        });

        HBox inputBox = new HBox(10, inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputBox.setPadding(new Insets(10));

        VBox root = new VBox(scrollPane, inputBox);
        root.setPrefSize(350, 450);

        Scene scene = new Scene(root);
        chatStage.setScene(scene);
        chatStage.show();
    }

    // ========== Message Handling ==========

    private void handleIncomingMessage(ChatMessageDTO message) {
        Platform.runLater(() -> {
            if (activeChatBox != null && message.getSenderId().equals(activeChatFriendId)) {
                Label friendLabel = new Label(message.getContent());
                friendLabel.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 8px; -fx-background-radius: 8px; -fx-border-color: -color-border-default; -fx-border-radius: 8px;");
                HBox friendRow = new HBox(friendLabel);
                friendRow.setAlignment(Pos.CENTER_LEFT);
                activeChatBox.getChildren().add(friendRow);
            } else {
                System.out.println("New message from user ID " + message.getSenderId() + ": " + message.getContent());
            }
        });
    }
}

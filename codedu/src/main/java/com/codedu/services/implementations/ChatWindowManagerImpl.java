package com.codedu.services.implementations;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.ChatMessageDTO;
import com.codedu.services.interfaces.ChatWindowManager;
import com.codedu.services.interfaces.WebSocketClientService;
import com.codedu.models.social.ChatMessage;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.ChatMessageRepository;
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

import java.util.List;

@Component
public class ChatWindowManagerImpl implements ChatWindowManager {

    private final WebSocketClientService webSocketClientService;
    private final ChatMessageRepository chatMessageRepository;
    private VBox activeChatBox;
    private String activeChatFriendId;
    private ScrollPane activeScrollPane;
    private User currentUser;

    public ChatWindowManagerImpl(WebSocketClientService webSocketClientService,
            ChatMessageRepository chatMessageRepository) {
        this.webSocketClientService = webSocketClientService;
        this.chatMessageRepository = chatMessageRepository;
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
        if (this.currentUser == null)
            return;

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
        this.activeScrollPane = scrollPane;

        // Load chat history from DB
        loadChatHistory(messageContainer, currentUser.getId(), friend.getId());

        sendButton.setOnAction(e -> {
            String text = inputField.getText();
            if (!text.trim().isEmpty()) {
                long now = System.currentTimeMillis();

                ChatMessageDTO msg = ChatMessageDTO.builder()
                        .senderId(String.valueOf(currentUser.getId()))
                        .receiverId(String.valueOf(friend.getId()))
                        .content(text)
                        .timestamp(now)
                        .build();

                // Send via WebSocket — server-side ChatMessageController persists to DB
                webSocketClientService.sendMessage(msg);

                // Show in local UI
                Label selfLabel = new Label(text);
                selfLabel.setStyle(
                        "-fx-background-color: -color-accent-subtle; -fx-padding: 8px; -fx-background-radius: 8px;");
                HBox selfRow = new HBox(selfLabel);
                selfRow.setAlignment(Pos.CENTER_RIGHT);
                messageContainer.getChildren().add(selfRow);

                inputField.clear();

                // Auto-scroll to bottom
                Platform.runLater(() -> {
                    scrollPane.layout();
                    scrollPane.setVvalue(1.0);
                });
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

        // Scroll to bottom after rendering
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ========== History ==========

    private void loadChatHistory(VBox messageContainer, int currentUserId, int friendId) {
        try {
            List<ChatMessage> history = chatMessageRepository.findConversation(currentUserId, friendId);
            for (ChatMessage msg : history) {
                Label label = new Label(msg.getContent());
                HBox row = new HBox(label);

                if (msg.getSenderId() == currentUserId) {
                    // My message
                    label.setStyle(
                            "-fx-background-color: -color-accent-subtle; -fx-padding: 8px; -fx-background-radius: 8px;");
                    row.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    // Friend's message
                    label.setStyle(
                            "-fx-background-color: -color-bg-subtle; -fx-padding: 8px; -fx-background-radius: 8px; -fx-border-color: -color-border-default; -fx-border-radius: 8px;");
                    row.setAlignment(Pos.CENTER_LEFT);
                }
                messageContainer.getChildren().add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ========== Message Handling ==========

    private void handleIncomingMessage(ChatMessageDTO message) {
        Platform.runLater(() -> {
            if (activeChatBox != null && message.getSenderId().equals(activeChatFriendId)) {
                Label friendLabel = new Label(message.getContent());
                friendLabel.setStyle(
                        "-fx-background-color: -color-bg-subtle; -fx-padding: 8px; -fx-background-radius: 8px; -fx-border-color: -color-border-default; -fx-border-radius: 8px;");
                HBox friendRow = new HBox(friendLabel);
                friendRow.setAlignment(Pos.CENTER_LEFT);
                activeChatBox.getChildren().add(friendRow);

                // Auto-scroll to bottom
                if (activeScrollPane != null) {
                    activeScrollPane.layout();
                    activeScrollPane.setVvalue(1.0);
                }
            } else {
                System.out.println("New message from user ID " + message.getSenderId() + ": " + message.getContent());
            }
        });
    }
}

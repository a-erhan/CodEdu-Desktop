package com.codedu.services.implementations;

import atlantafx.base.theme.Styles;
import com.codedu.services.interfaces.ChatWindowManager;
import com.codedu.services.interfaces.FriendshipUIManager;
import com.codedu.services.interfaces.UserService;
import com.codedu.models.user.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class FriendshipUIManagerImpl implements FriendshipUIManager {

    private final UserService userService;
    private final ChatWindowManager chatWindowManager;

    public FriendshipUIManagerImpl(UserService userService, ChatWindowManager chatWindowManager) {
        this.userService = userService;
        this.chatWindowManager = chatWindowManager;
    }

    // ========== Add Friend Button ==========

    public void setupAddFriendButton(Button addFriendButton, User currentUser, User profileUser, boolean viewingSelf) {
        if (addFriendButton == null)
            return;

        addFriendButton.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER);
        if (!addFriendButton.getStyleClass().contains(Styles.ACCENT)) {
            addFriendButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
        }

        if (viewingSelf) {
            addFriendButton.setVisible(false);
            addFriendButton.setManaged(false);
            return;
        }

        addFriendButton.setVisible(true);
        addFriendButton.setManaged(true);

        String relationStatus = "NONE";
        if (currentUser != null && profileUser != null) {
            try {
                relationStatus = userService.getRelationStatus(currentUser.getId(), profileUser.getId());
            } catch (Exception ignored) {
            }
        }

        switch (relationStatus) {
            case "PENDING":
                addFriendButton.setText("Request Sent");
                addFriendButton.setDisable(true);
                break;
            case "ACCEPTED":
                addFriendButton.setText("Friends");
                addFriendButton.getStyleClass().add(Styles.SUCCESS);
                addFriendButton.setDisable(true);
                break;
            case "BLOCKED":
                addFriendButton.setText("Blocked");
                addFriendButton.getStyleClass().add(Styles.DANGER);
                addFriendButton.setDisable(true);
                break;
            default:
                addFriendButton.setText("Add friend");
                addFriendButton.setDisable(false);
                addFriendButton.setOnAction(e -> handleSendFriendRequest(addFriendButton, currentUser, profileUser));
                break;
        }
    }

    private void handleSendFriendRequest(Button addFriendButton, User currentUser, User profileUser) {
        if (currentUser == null || profileUser == null)
            return;
        try {
            addFriendButton.setDisable(true);
            addFriendButton.setText("Request Sent");
            userService.sendFriendRequest(currentUser.getId(), profileUser.getId());
        } catch (Exception ex) {
            addFriendButton.setDisable(false);
            addFriendButton.setText("Add friend");
        }
    }

    // ========== Friends List & Pending Requests ==========

    public void renderFriendsList(VBox friendsList, Label noFriendsLabel, User currentUser, boolean viewingSelf,
            Runnable onRefresh, Consumer<User> onProfileClick) {
        if (!viewingSelf || friendsList == null)
            return;

        friendsList.getChildren().clear();

        List<User> pendingRequests = List.of();
        if (currentUser != null) {
            try {
                pendingRequests = userService.getPendingRequestEntities(currentUser.getId());
            } catch (Exception ignored) {
            }
        }

        if (!pendingRequests.isEmpty()) {
            Label pendingTitle = new Label("Pending Requests (" + pendingRequests.size() + ")");
            pendingTitle.getStyleClass().addAll(Styles.TEXT_MUTED, Styles.TEXT_BOLD);
            VBox.setMargin(pendingTitle, new Insets(8, 0, 4, 0));
            friendsList.getChildren().add(pendingTitle);

            for (User requester : pendingRequests) {
                friendsList.getChildren().add(createPendingRequestRow(requester, currentUser, onRefresh));
            }

            Region separator = new Region();
            separator.setMinHeight(1);
            separator.setStyle("-fx-background-color: -color-border-default;");
            VBox.setMargin(separator, new Insets(12, 0, 12, 0));
            friendsList.getChildren().add(separator);

            Label friendsTitle = new Label("My Friends");
            friendsTitle.getStyleClass().addAll(Styles.TEXT_MUTED, Styles.TEXT_BOLD);
            VBox.setMargin(friendsTitle, new Insets(0, 0, 4, 0));
            friendsList.getChildren().add(friendsTitle);
        }

        List<User> friends = List.of();
        if (currentUser != null) {
            try {
                friends = userService.getAcceptedFriendEntities(currentUser.getId());
            } catch (Exception ignored) {
            }
        }

        if (friends.isEmpty()) {
            if (noFriendsLabel != null) {
                noFriendsLabel.setVisible(true);
                noFriendsLabel.setManaged(true);
            }
        } else {
            if (noFriendsLabel != null) {
                noFriendsLabel.setVisible(false);
                noFriendsLabel.setManaged(false);
            }
            for (User friend : friends) {
                friendsList.getChildren().add(createFriendRow(friend, onProfileClick));
            }
        }
    }

    private HBox createPendingRequestRow(User requester, User currentUser, Runnable onRefresh) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        String name = requester.getUsername() != null ? requester.getUsername() : "?";
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();

        Label avatarLabel = new Label(initial);
        avatarLabel.setMinSize(36, 36);
        avatarLabel.setPrefSize(36, 36);
        avatarLabel.setMaxSize(36, 36);
        avatarLabel.setAlignment(Pos.CENTER);
        avatarLabel.setShape(new Circle(18));
        avatarLabel.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED);
        avatarLabel.setStyle("-fx-background-color: -color-warning-emphasis; -fx-text-fill: white;");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED, Styles.SUCCESS, Styles.SMALL);
        acceptBtn.setOnAction(e -> handleAnswerRequest(currentUser, requester, true, onRefresh));

        Button rejectBtn = new Button("Reject");
        rejectBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED, Styles.DANGER, Styles.SMALL);
        rejectBtn.setOnAction(e -> handleAnswerRequest(currentUser, requester, false, onRefresh));

        HBox btnBox = new HBox(8, acceptBtn, rejectBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(avatarLabel, nameLabel, spacer, btnBox);
        return row;
    }

    private void handleAnswerRequest(User currentUser, User requester, boolean accept, Runnable onRefresh) {
        try {
            userService.answerFriendRequest(currentUser.getId(), requester.getId(), accept);
            if (onRefresh != null)
                onRefresh.run(); // UI'ı yenilemek için ProfileController'ı tetikler
        } catch (Exception ignored) {
        }
    }

    private HBox createFriendRow(User friend, Consumer<User> onProfileClick) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        String name = friend.getUsername() != null ? friend.getUsername() : "?";
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();

        Label avatarLabel = new Label(initial);
        avatarLabel.setMinSize(36, 36);
        avatarLabel.setPrefSize(36, 36);
        avatarLabel.setMaxSize(36, 36);
        avatarLabel.setAlignment(Pos.CENTER);
        avatarLabel.setShape(new Circle(18));
        avatarLabel.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED);
        avatarLabel.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: white;");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);

        // Click on avatar, name, or the whole row -> navigate to friend's profile
        if (onProfileClick != null) {
            row.setCursor(Cursor.HAND);
            row.setOnMouseClicked(e -> {
                // If the user didn't click the Chat button specifically
                if (!(e.getTarget() instanceof Button)) {
                    onProfileClick.accept(friend);
                }
            });
            avatarLabel.setCursor(Cursor.HAND);
            nameLabel.setCursor(Cursor.HAND);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button chatButton = new Button("Chat");
        chatButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED, Styles.ACCENT, Styles.SMALL);
        chatButton.setOnAction(e -> chatWindowManager.openChatWindow(friend));

        row.getChildren().addAll(avatarLabel, nameLabel, spacer, chatButton);
        return row;
    }
}
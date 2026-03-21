package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.services.UserService;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    // ========== FXML bindings ==========
    @FXML
    private Label avatarDisplay;
    @FXML
    private Label usernameDisplay;
    @FXML
    private Label badgeDisplay;
    @FXML
    private ProgressBar profileXpBar;
    @FXML
    private Label profileXpLabel;
    @FXML
    private Label profileTokenLabel;
    @FXML
    private Label profileItemsLabel;
    @FXML
    private VBox avatarsSection;
    @FXML
    private VBox avatarCard;
    @FXML
    private VBox xpCard;
    @FXML
    private VBox tokensCard;
    @FXML
    private VBox itemsCard;
    @FXML
    private Button addFriendButton;
    @FXML
    private Label noAvatarsLabel;
    @FXML
    private FlowPane avatarGrid;
    @FXML
    private VBox friendsSection;
    @FXML
    private Label friendsSectionTitle;
    @FXML
    private Label noFriendsLabel;
    @FXML
    private VBox friendsList;
    @FXML
    private VBox badgesSection;
    @FXML
    private Label badgesSectionTitle;
    @FXML
    private Label noBadgesLabel;
    @FXML
    private FlowPane badgesContainer;

    // ========== State ==========
    private User currentUser; // the logged-in user (self)
    private User profileUser; // the user whose profile is being viewed
    private UserGameState gameState;
    private boolean viewingSelf;

    // ========== Setters called from MainShellController ==========

    /**
     * Called when viewing own profile via the header icon.
     */
    public void setViewingSelf(boolean viewingSelf) {
        this.viewingSelf = viewingSelf;
    }

    /**
     * Sets the user model for the profile being viewed, and also stores it
     * as currentUser when viewing self.
     */
    public void setUserModel(User user) {
        this.profileUser = user;
        if (viewingSelf) {
            this.currentUser = user;
        }
        bindUI();
    }

    /**
     * Sets the game state for the profile being viewed.
     */
    public void setGameState(UserGameState gameState) {
        this.gameState = gameState;
        // Refresh stats if profile was already bound
        if (profileUser != null) {
            bindStats();
        }
    }

    /**
     * Called when navigating to a competitor's profile from the leaderboard.
     */
    public void setCompetitor(Competitor competitor, List<Competitor> competitorOrder) {
        this.viewingSelf = false;
        if (competitor != null && competitor.getUser() != null) {
            this.profileUser = competitor.getUser();
        }
        try {
            if (profileUser != null) {
                this.gameState = profileUser.getGameState();
            }
        } catch (Exception ignored) {
            // LazyInitializationException — gameState not loaded
        }
        if (this.gameState == null && profileUser != null) {
            this.gameState = UserGameState.builder()
                    .user(profileUser)
                    .level(1).xp(0).heartCount(3)
                    .build();
        }
        bindUI();
    }

    /**
     * Allows the calling controller to pass the logged-in user
     * so friend requests use the correct requester.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    // ========== UI binding ==========

    private void bindUI() {
        if (profileUser == null)
            return;

        String username = profileUser.getUsername() != null ? profileUser.getUsername() : "Unknown";
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();

        // Avatar
        avatarDisplay.setText(initial);
        avatarDisplay.setAlignment(Pos.CENTER);
        avatarDisplay.setMinSize(80, 80);
        avatarDisplay.setPrefSize(80, 80);
        avatarDisplay.setMaxSize(80, 80);
        avatarDisplay.setShape(new Circle(40));
        if (!avatarDisplay.getStyleClass().contains(Styles.TITLE_2)) {
            avatarDisplay.getStyleClass().add(Styles.TITLE_2);
        }

        // Username & level
        usernameDisplay.setText(username);
        if (!usernameDisplay.getStyleClass().contains(Styles.TITLE_3)) {
            usernameDisplay.getStyleClass().add(Styles.TITLE_3);
        }

        bindStats();
        bindAddFriendButton();
        bindFriendsSection();
        bindAvatarsSection();
        applyCardStyles();
    }

    private void bindStats() {
        int level = 1, xp = 0, targetXp = 1000, tokens = 0, items = 0;

        if (gameState != null) {
            level = gameState.getLevel();
            xp = gameState.getXp();
            targetXp = gameState.getXpToNextLevel();
            tokens = gameState.getTokenBalance();
        } else if (profileUser != null) {
            try {
                UserGameState gs = profileUser.getGameState();
                if (gs != null) {
                    level = gs.getLevel();
                    xp = gs.getXp();
                    targetXp = gs.getXpToNextLevel();
                    tokens = gs.getTokenBalance();
                }
            } catch (Exception ignored) {
            }
        }

        if (profileUser != null) {
            try {
                if (profileUser.getInventory() != null && profileUser.getInventory().getItems() != null) {
                    items = profileUser.getInventory().getItems().size();
                }
            } catch (Exception ignored) {
            }
        }

        badgeDisplay.setText("Level " + level);
        double progress = targetXp == 0 ? 0 : Math.min(1.0, (double) xp / (level * 1000));
        profileXpBar.setProgress(progress);
        profileXpLabel.setText(xp + " / " + (level * 1000) + " XP");
        profileTokenLabel.setText(String.valueOf(tokens));
        profileItemsLabel.setText(String.valueOf(items));
    }

    private void bindAddFriendButton() {
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

        // Determine relationship status
        String relationStatus = "NONE";
        if (currentUser != null && profileUser != null && userService != null) {
            try {
                relationStatus = userService.getRelationStatus(currentUser, profileUser);
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
            default: // NONE
                addFriendButton.setText("Add friend");
                addFriendButton.setDisable(false);
                addFriendButton.setOnAction(e -> handleSendFriendRequest());
                break;
        }
    }

    private void handleSendFriendRequest() {
        if (currentUser == null || profileUser == null) {
            System.err.println("Cannot send friend request: current or target user is null.");
            return;
        }

        try {
            addFriendButton.setDisable(true);
            addFriendButton.setText("Request Sent");

            if (userService != null) {
                userService.sendFriendRequest(currentUser, profileUser);
            }

            System.out.println("Friend request sent to: " + profileUser.getUsername());
        } catch (Exception ex) {
            addFriendButton.setDisable(false);
            addFriendButton.setText("Add friend");
            System.err.println("Error sending friend request: " + ex.getMessage());
        }
    }

    private void bindFriendsSection() {
        if (friendsSection == null)
            return;

        friendsSection.setVisible(viewingSelf);
        friendsSection.setManaged(viewingSelf);

        if (viewingSelf && friendsSectionTitle != null) {
            if (!friendsSectionTitle.getStyleClass().contains(Styles.TITLE_3)) {
                friendsSectionTitle.getStyleClass().add(Styles.TITLE_3);
            }
        }

        if (!viewingSelf || friendsList == null)
            return;

        friendsList.getChildren().clear();

        List<User> friends = List.of();
        if (userService != null && currentUser != null) {
            try {
                friends = userService.getAcceptedFriends(currentUser);
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
                friendsList.getChildren().add(createFriendRow(friend));
            }
        }
    }

    private HBox createFriendRow(User friend) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        // Friend avatar initial
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

        // Friend name
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Chat button
        Button chatButton = new Button("💬 Chat");
        chatButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED, Styles.ACCENT, Styles.SMALL);
        chatButton.setOnAction(e -> {
            System.out.println("Opening chat with: " + friend.getUsername());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chat");
            alert.setHeaderText(null);
            alert.setContentText("Chat with " + friend.getUsername() + " coming soon!");
            alert.showAndWait();
        });

        row.getChildren().addAll(avatarLabel, nameLabel, spacer, chatButton);
        return row;
    }

    private void bindAvatarsSection() {
        if (avatarsSection != null) {
            avatarsSection.setVisible(viewingSelf);
            avatarsSection.setManaged(viewingSelf);
        }
    }

    private void applyCardStyles() {
        if (avatarCard != null)
            avatarCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE, Styles.ELEVATED_1);
        if (xpCard != null)
            xpCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        if (tokensCard != null)
            tokensCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        if (itemsCard != null)
            itemsCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
    }
}
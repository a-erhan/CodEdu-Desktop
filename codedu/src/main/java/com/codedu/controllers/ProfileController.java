package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.InventoryItem;
import com.codedu.models.User;
import com.codedu.models.UserGameState;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Profile view.
 * Displays user stats.
 */
@Controller
public class ProfileController {

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
    private Label profileBadgeLabel;
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
    private VBox badgeCard;
    @FXML
    private VBox itemsCard;
    @FXML
    private Button addFriendButton;
    @FXML
    private Label noAvatarsLabel;
    @FXML
    private FlowPane avatarGrid;

    private User user;
    private UserGameState gameState;
    private boolean viewingSelf = true;

    public void setGameState(UserGameState gameState) {
        this.gameState = gameState;
        bindStats();
    }

    public void setUserModel(User user) {
        this.user = user;
        bindStats();
    }

    public void setViewingSelf(boolean viewingSelf) {
        this.viewingSelf = viewingSelf;
        bindStats();
    }

    private void bindStats() {
        String username = user != null && user.getUsername() != null ? user.getUsername() : "User";
        String initial = username.isEmpty() ? "C" : username.substring(0, 1).toUpperCase();
        avatarDisplay.setText(initial);
        avatarDisplay.getStyleClass().add(Styles.TITLE_2);
        usernameDisplay.setText(username);
        usernameDisplay.getStyleClass().add(Styles.TITLE_3);
        int level = gameState != null ? gameState.getLevel() : 1;
        badgeDisplay.setText("Level " + level);

        int xp = gameState != null ? gameState.getXp() : 0;
        int levelCap = Math.max(1, level * 1000);
        double progress = Math.max(0, Math.min(1, (double) xp / levelCap));
        profileXpBar.setProgress(progress);
        profileXpLabel.setText(xp + " / " + levelCap + " XP");

        int tokens = user != null ? user.getTokenBalance() : 0;
        profileTokenLabel.setText(String.valueOf(tokens));
        profileBadgeLabel.setText("Level " + level);

        int itemCount = 0;
        if (user != null && user.getInventory() != null && user.getInventory().getItems() != null) {
            for (InventoryItem inv : user.getInventory().getItems()) {
                itemCount += Math.max(1, inv.getQuantity());
            }
        }
        profileItemsLabel.setText(String.valueOf(itemCount));

        if (addFriendButton != null) {
            if (viewingSelf) {
                addFriendButton.setVisible(false);
                addFriendButton.setManaged(false);
            } else {
                addFriendButton.setVisible(true);
                addFriendButton.setManaged(true);
                if (!addFriendButton.getStyleClass().contains(Styles.ACCENT)) {
                    addFriendButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
                }
                addFriendButton.setDisable(false);
                addFriendButton.setText("Add friend");
                addFriendButton.setOnAction(e -> {
                    addFriendButton.setDisable(true);
                    addFriendButton.setText("Request sent");
                });
            }
        }

        // Show "My avatars" only when viewing own profile
        if (avatarsSection != null) {
            boolean showAvatars = viewingSelf;
            avatarsSection.setVisible(showAvatars);
            avatarsSection.setManaged(showAvatars);
        }
        if (noAvatarsLabel != null) {
            noAvatarsLabel.setVisible(true);
            noAvatarsLabel.setManaged(true);
        }
        if (avatarGrid != null) {
            avatarGrid.getChildren().clear();
        }

        // Card-like styling for profile sections
        if (avatarCard != null) {
            avatarCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE, Styles.ELEVATED_1);
        }
        if (xpCard != null) {
            xpCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (tokensCard != null) {
            tokensCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (badgeCard != null) {
            badgeCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (itemsCard != null) {
            itemsCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
    }
}

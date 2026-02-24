package com.codedu.controllers;

import com.codedu.models.User;
import com.codedu.models.InventoryItem;
import com.codedu.models.UserGameState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
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
    private Label noAvatarsLabel;
    @FXML
    private FlowPane avatarGrid;

    private User user;
    private UserGameState gameState;

    public void setGameState(UserGameState gameState) {
        this.gameState = gameState;
        bindStats();
    }

    public void setUserModel(User user) {
        this.user = user;
        bindStats();
    }

    private void bindStats() {
        String username = user != null && user.getUsername() != null ? user.getUsername() : "User";
        String initial = username.isEmpty() ? "C" : username.substring(0, 1).toUpperCase();
        avatarDisplay.setText(initial);
        usernameDisplay.setText(username);
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

        noAvatarsLabel.setVisible(true);
        noAvatarsLabel.setManaged(true);
        avatarGrid.getChildren().clear();
    }
}

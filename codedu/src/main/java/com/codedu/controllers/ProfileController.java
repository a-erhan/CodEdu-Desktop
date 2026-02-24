package com.codedu.controllers;

import com.codedu.models.User;
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

    public void setUserModel(User user) {
        this.user = user;
        bindStats();
    }

    private void bindStats() {
        String username = user.getUsername() != null ? user.getUsername() : "User";
        String initial = username.isEmpty() ? "C" : username.substring(0, 1).toUpperCase();
        avatarDisplay.setText(initial);
        avatarDisplay.setStyle("-fx-font-size: 72px;");
        usernameDisplay.setText(username);
        badgeDisplay.setText("Level 1");

        profileXpBar.setProgress(0);
        profileXpLabel.setText("0 / 1000 XP");
        profileTokenLabel.setText(String.valueOf(user.getTokenBalance()));
        profileBadgeLabel.setText("Level 1");
        profileItemsLabel.setText("0");

        noAvatarsLabel.setVisible(true);
        noAvatarsLabel.setManaged(true);
        avatarGrid.getChildren().clear();
    }
}

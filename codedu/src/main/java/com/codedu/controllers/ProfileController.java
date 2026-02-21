package com.codedu.controllers;

import com.codedu.models.StoreItem;
import com.codedu.models.UserModel;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the Profile view.
 * Displays user stats and owned avatar cosmetics that can be equipped.
 */
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

    private UserModel user;

    public void setUserModel(UserModel user) {
        this.user = user;
        bindStats();
        buildAvatarGrid();

        // Listen for changes
        user.getOwnedItems().addListener((javafx.collections.ListChangeListener<StoreItem>) c -> buildAvatarGrid());
        user.equippedAvatarProperty().addListener((obs, o, n) -> {
            avatarDisplay.setText(n);
            buildAvatarGrid();
        });
        user.tokenBalanceProperty().addListener((obs, o, n) -> profileTokenLabel.setText(String.valueOf(n.intValue())));
    }

    private void bindStats() {
        avatarDisplay.setText(user.getEquippedAvatar());
        avatarDisplay.setStyle("-fx-font-size: 72px;");
        usernameDisplay.setText(user.getUsername());
        badgeDisplay.setText(user.getBadgeIcon() + "  Level 7 — Code Warrior");

        profileXpBar.setProgress(user.getXPProgress());
        profileXpLabel.setText(user.getCurrentXP() + " / " + user.getMaxXP() + " XP");
        profileTokenLabel.setText(String.valueOf(user.getTokenBalance()));
        profileBadgeLabel.setText(user.getBadgeIcon());
        profileItemsLabel.setText(String.valueOf(user.getOwnedItems().size()));
    }

    private void buildAvatarGrid() {
        avatarGrid.getChildren().clear();
        long avatarCount = user.getOwnedItems().stream()
                .filter(i -> i.getCategory() == StoreItem.Category.AVATAR)
                .count();

        noAvatarsLabel.setVisible(avatarCount == 0);
        noAvatarsLabel.setManaged(avatarCount == 0);
        profileItemsLabel.setText(String.valueOf(user.getOwnedItems().size()));

        for (StoreItem item : user.getOwnedItems()) {
            if (item.getCategory() != StoreItem.Category.AVATAR)
                continue;

            boolean isEquipped = user.getEquippedAvatar().equals(item.getEmoji());

            VBox cell = new VBox(6);
            cell.getStyleClass().add("profile-avatar-cell");
            if (isEquipped)
                cell.getStyleClass().add("profile-avatar-cell-active");
            cell.setPadding(new Insets(14, 14, 10, 14));
            cell.setAlignment(Pos.CENTER);
            cell.setPrefWidth(120);

            Label emoji = new Label(item.getEmoji());
            emoji.setStyle("-fx-font-size: 36px;");

            Label name = new Label(item.getName());
            name.getStyleClass().add("profile-avatar-name");
            name.setWrapText(true);

            Button equipBtn = new Button(isEquipped ? "✓ Equipped" : "Equip");
            equipBtn.getStyleClass().add(isEquipped ? "profile-equipped-btn" : "profile-equip-btn");
            equipBtn.setMaxWidth(Double.MAX_VALUE);

            if (!isEquipped) {
                equipBtn.setOnAction(e -> {
                    user.equipAvatar(item);
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), cell);
                    st.setToX(1.1);
                    st.setToY(1.1);
                    st.setAutoReverse(true);
                    st.setCycleCount(2);
                    st.play();
                });
            }

            // Hover animation
            cell.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), cell);
                st.setToX(1.05);
                st.setToY(1.05);
                st.play();
            });
            cell.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), cell);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            cell.getChildren().addAll(emoji, name, equipBtn);
            avatarGrid.getChildren().add(cell);
        }
    }
}

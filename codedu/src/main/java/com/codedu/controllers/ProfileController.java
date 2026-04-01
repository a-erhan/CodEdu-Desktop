package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.gamification.Achievement;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.services.interfaces.ChatWindowManager;
import com.codedu.services.interfaces.FriendshipUIManager;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for the Profile view.
 * Displays user stats, badges, and friend list.
 */
@Controller
public class ProfileController {

    @Autowired
    private ChatWindowManager chatWindowManager;

    @Autowired
    private FriendshipUIManager friendshipUIManager;

    // ========== FXML bindings ==========
    @FXML private Label avatarDisplay;
    @FXML private Label usernameDisplay;
    @FXML private Label badgeDisplay;
    @FXML private ProgressBar profileXpBar;
    @FXML private Label profileXpLabel;
    @FXML private Label profileTokenLabel;
    @FXML private Label profileItemsLabel;
    @FXML private VBox avatarsSection;
    @FXML private VBox avatarCard; 
    @FXML private VBox xpCard;
    @FXML private VBox tokensCard;
    @FXML private VBox itemsCard;
    @FXML private Button addFriendButton;
    @FXML private Label noAvatarsLabel;
    @FXML private FlowPane avatarGrid;
    @FXML private VBox friendsSection;
    @FXML private Label friendsSectionTitle;
    @FXML private Label noFriendsLabel;
    @FXML private VBox friendsList;
    @FXML private VBox badgesSection;
    @FXML private Label badgesSectionTitle;
    @FXML private Label noBadgesLabel;
    @FXML private FlowPane badgesContainer;

    // ========== State ==========
    private User currentUser;
    private User profileUser;
    private UserGameState gameState;
    private boolean viewingSelf;
    private Consumer<User> onProfileClick;

    // ========== Setters ==========

    public void setViewingSelf(boolean viewingSelf) {
        this.viewingSelf = viewingSelf;
    }

    public void setUserModel(User user) {
        this.profileUser = user;
        if (viewingSelf) {
            this.currentUser = user;
        }
        bindUI();
    }

    public void setGameState(UserGameState gameState) {
        this.gameState = gameState;
        if (profileUser != null) {
            bindStats();
        }
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        chatWindowManager.connectUser(currentUser);
    }

    public void setOnProfileClick(Consumer<User> onProfileClick) {
        this.onProfileClick = onProfileClick;
    }

    public void setCompetitor(Competitor competitor, List<Competitor> competitorOrder) {
        this.viewingSelf = false;
        if (competitor != null && competitor.getUser() != null) {
            this.profileUser = competitor.getUser();
        }
        try {
            if (profileUser != null) {
                this.gameState = profileUser.getGameState();
            }
        } catch (Exception ignored) {}

        if (this.gameState == null && profileUser != null) {
            this.gameState = UserGameState.builder()
                    .user(profileUser)
                    .level(1).xp(0).heartCount(3)
                    .build();
        }
        bindUI();
    }

    // ========== UI Binding ==========

    private void bindUI() {
        if (profileUser == null) return;

        String username = profileUser.getUsername() != null ? profileUser.getUsername() : "Unknown";
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();

        // Avatar styling
        avatarDisplay.setText(initial);
        avatarDisplay.setAlignment(Pos.CENTER);
        avatarDisplay.setMinSize(100, 100);
        avatarDisplay.setShape(new Circle(50));
        avatarDisplay.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.15); -fx-border-width: 2; -fx-border-radius: 50;");
        
        if (!avatarDisplay.getStyleClass().contains(Styles.TITLE_2)) {
            avatarDisplay.getStyleClass().add(Styles.TITLE_2);
        }

        usernameDisplay.setText(username);
        usernameDisplay.setStyle("-fx-text-fill: #D35400; -fx-font-weight: bold; -fx-font-size: 26px;");

        profileXpBar.setStyle(
            "-fx-accent: #D35400; " + 
            "-fx-control-inner-background: #1E272E; " + 
            "-fx-background-radius: 15; " + 
            "-fx-border-radius: 15; " +
            "-fx-min-height: 24;"
        );

        bindStats();
        bindBadgesSection();
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
            targetXp = gameState.getXpToNextLevel() > 0 ? gameState.getXpToNextLevel() : level * 1000;
            tokens = gameState.getTokenBalance();
        }

        if (profileUser != null && profileUser.getInventory() != null) {
            items = profileUser.getInventory().getItems() != null ? profileUser.getInventory().getItems().size() : 0;
        }

        badgeDisplay.setText("Level " + level);
        double progress = targetXp == 0 ? 0 : Math.min(1.0, (double) xp / targetXp);
        profileXpBar.setProgress(progress);
        profileXpLabel.setText(xp + " / " + targetXp + " XP");
        
        profileTokenLabel.setText(String.valueOf(tokens));
        profileTokenLabel.setStyle("-fx-text-fill: #F1C40F;");
        profileItemsLabel.setText(String.valueOf(items));
    }

    private void bindBadgesSection() {
        if (badgesSection == null || badgesContainer == null) return;

        if (badgesSectionTitle != null && !badgesSectionTitle.getStyleClass().contains(Styles.TITLE_3)) {
            badgesSectionTitle.getStyleClass().add(Styles.TITLE_3);
        }

        badgesContainer.getChildren().clear();
        List<Achievement> earned = (gameState != null) ? gameState.getAchievements() : null;

        if (earned == null || earned.isEmpty()) {
            noBadgesLabel.setVisible(true);
            noBadgesLabel.setManaged(true);
        } else {
            noBadgesLabel.setVisible(false);
            noBadgesLabel.setManaged(false);

            for (Achievement a : earned) {
                VBox badgePod = new VBox(10);
                badgePod.setAlignment(Pos.CENTER);
                badgePod.setPadding(new javafx.geometry.Insets(15));
                badgePod.setPrefSize(110, 130);
                
                badgePod.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.03); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: rgba(211, 84, 0, 0.3); " +
                    "-fx-border-radius: 20; " +
                    "-fx-border-width: 1;"
                );

                Label trophyIcon = new Label("🏆"); 
                trophyIcon.setStyle("-fx-font-size: 32px; -fx-effect: dropshadow(two-pass-box, rgba(211, 84, 0, 0.4), 10, 0.5, 0, 0);");

                Label badgeName = new Label(a.getName());
                badgeName.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 11px; -fx-font-weight: bold;");
                badgeName.setWrapText(true);
                badgeName.setAlignment(Pos.CENTER);

                badgePod.getChildren().addAll(trophyIcon, badgeName);
                badgesContainer.getChildren().add(badgePod);
            }
        }
    }

    private void bindAddFriendButton() {
        friendshipUIManager.setupAddFriendButton(addFriendButton, currentUser, profileUser, viewingSelf);
    }

    private void bindFriendsSection() {
        if (friendsSection != null) {
            friendsSection.setVisible(viewingSelf);
            friendsSection.setManaged(viewingSelf);
        }
        if (viewingSelf && friendsSectionTitle != null && !friendsSectionTitle.getStyleClass().contains(Styles.TITLE_3)) {
            friendsSectionTitle.getStyleClass().add(Styles.TITLE_3);
        }

        friendshipUIManager.renderFriendsList(friendsList, noFriendsLabel, currentUser, viewingSelf,
                this::bindFriendsSection, onProfileClick);
    }

    private void bindAvatarsSection() {
        if (avatarsSection != null) {
            avatarsSection.setVisible(viewingSelf);
            avatarsSection.setManaged(viewingSelf);
        }
    }

    private void applyCardStyles() {
        String smoothRadius = "22"; 
        String softerOrange = "#D35400";
        
        // Avatar Card Main Style
        if (avatarCard != null) {
            avatarCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE, Styles.ELEVATED_1);
            avatarCard.setStyle(
                "-fx-background-color: #1A1E23; " + 
                "-fx-background-radius: " + smoothRadius + "; " +
                "-fx-border-color: " + softerOrange + "; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: " + smoothRadius + "; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);" 
            ); 
        }
        
        // Dashboard Cards (XP, Tokens, Items) with Hover Effects
        VBox[] dashboardCards = {xpCard, tokensCard, itemsCard};
        for (VBox card : dashboardCards) {
            if (card == null) continue;
            
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
            card.setStyle(
                "-fx-background-color: #1E2329; " +
                "-fx-border-color: #34495E; " + 
                "-fx-border-width: 1.2; " +
                "-fx-background-radius: " + smoothRadius + "; " +
                "-fx-border-radius: " + smoothRadius + ";"
            );
            
            card.setOnMouseEntered(event -> card.setStyle(
                "-fx-background-color: #242B33; " +
                "-fx-border-color: " + softerOrange + "; " + 
                "-fx-border-width: 1.2; " +
                "-fx-background-radius: " + smoothRadius + "; " +
                "-fx-border-radius: " + smoothRadius + ";"
            ));

            card.setOnMouseExited(event -> card.setStyle(
                "-fx-background-color: #1E2329; " +
                "-fx-border-color: #34495E; " + 
                "-fx-border-width: 1.2; " +
                "-fx-background-radius: " + smoothRadius + "; " +
                "-fx-border-radius: " + smoothRadius + ";"
            ));
        }
    }
}
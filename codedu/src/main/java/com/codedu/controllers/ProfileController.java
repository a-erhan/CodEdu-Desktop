package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.services.ChatWindowManager;
import com.codedu.services.FriendshipUIManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.function.Consumer;
import com.codedu.models.gamification.Achievement;
import com.codedu.services.AchievementEvaluationService;

@Controller
public class ProfileController {

    @Autowired
    private ChatWindowManager chatWindowManager;

    @Autowired
    private FriendshipUIManager friendshipUIManager;

    @Autowired
    private AchievementEvaluationService achievementEvaluationService;

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
    private User currentUser;
    private User profileUser;
    private UserGameState gameState;
    private boolean viewingSelf;
    private Consumer<User> onProfileClick;

    // ========== Setters called from MainShellController ==========

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
        }

        if (this.gameState == null && profileUser != null) {
            this.gameState = UserGameState.builder()
                    .user(profileUser)
                    .level(1).xp(0).heartCount(3)
                    .build();
        }
        bindUI();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        chatWindowManager.connectUser(currentUser);
    }

    public void setOnProfileClick(Consumer<User> onProfileClick) {
        this.onProfileClick = onProfileClick;
    }

    // ========== UI binding ==========

    private void bindUI() {
        if (profileUser == null)
            return;

        // Silently retro-fetch XP/Tokens if the account's state desynced
        if (viewingSelf) {
            achievementEvaluationService.fixMissingXPOnLogin(profileUser);
        }

        String username = profileUser.getUsername() != null ? profileUser.getUsername() : "Unknown";
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();

        avatarDisplay.setText(initial);
        avatarDisplay.setAlignment(Pos.CENTER);
        avatarDisplay.setMinSize(80, 80);
        avatarDisplay.setPrefSize(80, 80);
        avatarDisplay.setMaxSize(80, 80);
        avatarDisplay.setShape(new Circle(40));
        if (!avatarDisplay.getStyleClass().contains(Styles.TITLE_2)) {
            avatarDisplay.getStyleClass().add(Styles.TITLE_2);
        }

        usernameDisplay.setText(username);
        if (!usernameDisplay.getStyleClass().contains(Styles.TITLE_3)) {
            usernameDisplay.getStyleClass().add(Styles.TITLE_3);
        }

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

    private void bindBadgesSection() {
        if (badgesSection == null || badgesContainer == null || badgesSectionTitle == null)
            return;

        if (!badgesSectionTitle.getStyleClass().contains(Styles.TITLE_3)) {
            badgesSectionTitle.getStyleClass().add(Styles.TITLE_3);
        }

        badgesContainer.getChildren().clear();

        List<Achievement> earnedAchievements = null;
        if (profileUser != null) {
            earnedAchievements = achievementEvaluationService.getUserAchievementsWithBadges(profileUser);
        } else if (gameState != null) {
            earnedAchievements = gameState.getAchievements();
        }

        if (earnedAchievements == null || earnedAchievements.isEmpty()) {
            noBadgesLabel.setVisible(true);
            noBadgesLabel.setManaged(true);
        } else {
            noBadgesLabel.setVisible(false);
            noBadgesLabel.setManaged(false);

            for (Achievement a : earnedAchievements) {
                javafx.scene.layout.VBox badgeWrapper = new javafx.scene.layout.VBox();
                badgeWrapper.setAlignment(Pos.CENTER);

                javafx.scene.image.ImageView badgeIcon = new javafx.scene.image.ImageView();
                try {
                    String rawPath = a.getBadge().getIconURL();
                    if (rawPath == null)
                        rawPath = "badge.png";
                    String fileName = java.nio.file.Paths.get(rawPath).getFileName().toString();
                    String iconPath = "/assets/badges/" + fileName;

                    java.net.URL resource = getClass().getResource(iconPath);
                    if (resource != null) {
                        javafx.scene.image.Image img = new javafx.scene.image.Image(resource.toExternalForm());
                        badgeIcon.setImage(img);
                        badgeIcon.setFitWidth(60);
                        badgeIcon.setFitHeight(60);
                        badgeIcon.setPreserveRatio(true);
                    } else {
                        System.err.println("Badge image not found: " + iconPath);
                    }
                } catch (Exception e) {
                    System.err.println("Could not load badge image: " + e.getMessage());
                }

                // Add a tooltip for the badge title and description!
                javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                        a.getBadge().getTitle() + "\n" + a.getBadge().getDescription());
                tooltip.setShowDelay(javafx.util.Duration.millis(200));
                javafx.scene.control.Tooltip.install(badgeIcon, tooltip);

                badgeWrapper.getChildren().add(badgeIcon);
                badgesContainer.getChildren().add(badgeWrapper);
            }
        }
    }

    // YENİ: Bütün karmaşık mantık tek bir servise devredildi!
    private void bindAddFriendButton() {
        friendshipUIManager.setupAddFriendButton(addFriendButton, currentUser, profileUser, viewingSelf);
    }

    // YENİ: Liste çizimi tamamen servise bırakıldı. this::bindFriendsSection ile
    // anlık yenilenme sağlandı.
    private void bindFriendsSection() {
        if (friendsSection != null) {
            friendsSection.setVisible(viewingSelf);
            friendsSection.setManaged(viewingSelf);
        }

        if (viewingSelf && friendsSectionTitle != null) {
            if (!friendsSectionTitle.getStyleClass().contains(Styles.TITLE_3)) {
                friendsSectionTitle.getStyleClass().add(Styles.TITLE_3);
            }
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
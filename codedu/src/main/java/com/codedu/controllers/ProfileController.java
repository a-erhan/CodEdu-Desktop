package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.Competitor;
import com.codedu.models.InventoryItem;
import com.codedu.models.User;
import com.codedu.models.UserGameState;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
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

    /**
     * Show another user's profile from a leaderboard competitor.
     * Builds display user and game state from competitor; use when opening profile from Leaderboard.
     */
    public void setCompetitor(Competitor competitor, List<Competitor> leaderboardOrder) {
        if (competitor == null) return;
        User profileUser = competitor.getUser();
        if (profileUser == null && leaderboardOrder != null) {
            int idx = Math.max(0, leaderboardOrder.indexOf(competitor));
            profileUser = User.builder()
                    .username("Player " + (idx + 1))
                    .email("")
                    .password("")
                    .build();
        } else if (profileUser == null) {
            profileUser = User.builder().username("Player").email("").password("").build();
        }
        int ranking = competitor.getRankingPoint();
        int level = Math.max(1, ranking / 1000);
        UserGameState otherState = UserGameState.builder()
                .user(profileUser)
                .level(level)
                .xp(ranking)
                .heartCount(3)
                .build();
        this.user = profileUser;
        this.gameState = otherState;
        this.viewingSelf = false;
        bindStats();
    }

    private void bindStats() {
        String username = user != null && user.getUsername() != null ? user.getUsername() : "User";
        String initial = username.isEmpty() ? "C" : username.substring(0, 1).toUpperCase();
        avatarDisplay.setText(initial);
        avatarDisplay.setAlignment(Pos.CENTER);
        avatarDisplay.setMinSize(80, 80);
        avatarDisplay.setPrefSize(80, 80);
        avatarDisplay.setMaxSize(80, 80);
        avatarDisplay.setShape(new Circle(40));
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

        // Badges section: visible to everyone (showcase to friends)
        if (badgesSection != null) {
            badgesSection.setVisible(true);
            badgesSection.setManaged(true);
            if (badgesSectionTitle != null) badgesSectionTitle.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);
            if (noBadgesLabel != null) noBadgesLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
            buildBadgesList();
        }

        // Friends section: only when viewing own profile
        if (friendsSection != null) {
            friendsSection.setVisible(viewingSelf);
            friendsSection.setManaged(viewingSelf);
            if (viewingSelf) {
                if (friendsSectionTitle != null) friendsSectionTitle.getStyleClass().add(Styles.TITLE_3);
                buildFriendsList();
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
        if (itemsCard != null) {
            itemsCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
    }

    /** Build the friends list (demo data when viewing self). */
    private void buildFriendsList() {
        if (friendsList == null) return;
        friendsList.getChildren().clear();

        java.util.List<User> friends = getFriendsForCurrentUser();
        if (noFriendsLabel != null) {
            noFriendsLabel.setVisible(friends.isEmpty());
            noFriendsLabel.setManaged(friends.isEmpty());
        }
        for (User friend : friends) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
            row.setPadding(new javafx.geometry.Insets(10, 14, 10, 14));

            Label initial = new Label("");
            String name = friend.getUsername() != null ? friend.getUsername() : "Friend";
            String init = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            initial.setText(init);
            initial.setMinSize(36, 36);
            initial.setPrefSize(36, 36);
            initial.setMaxSize(36, 36);
            initial.setAlignment(Pos.CENTER);
            initial.setShape(new Circle(18));
            initial.getStyleClass().add(Styles.TEXT_BOLD);

            Label usernameLabel = new Label(name);
            usernameLabel.getStyleClass().add(Styles.TEXT_BOLD);

            row.getChildren().addAll(initial, usernameLabel);
            friendsList.getChildren().add(row);
        }
    }

    /** Demo friends list (replace with real data when backend exists). */
    private java.util.List<User> getFriendsForCurrentUser() {
        java.util.List<User> list = new java.util.ArrayList<>();
        if (!viewingSelf || user == null) return list;
        list.add(User.builder().username("CodeMaster").email("").password("").build());
        list.add(User.builder().username("ByteNinja").email("").password("").build());
        list.add(User.builder().username("DevExplorer").email("").password("").build());
        return list;
    }

    /** Build the badges list (demo data; replace with user's achievements when wired to backend). */
    private void buildBadgesList() {
        if (badgesContainer == null) return;
        badgesContainer.getChildren().clear();

        java.util.List<BadgeDisplay> badges = getBadgesForUser();
        if (noBadgesLabel != null) {
            noBadgesLabel.setVisible(badges.isEmpty());
            noBadgesLabel.setManaged(badges.isEmpty());
        }
        for (BadgeDisplay b : badges) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_CENTER);
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE, Styles.ELEVATED_1);
            card.setPadding(new javafx.geometry.Insets(14, 18, 14, 18));
            card.setMinWidth(120);
            card.setMaxWidth(160);

            Label icon = new Label(b.icon);
            icon.getStyleClass().add(Styles.TITLE_2);
            Label name = new Label(b.name);
            name.getStyleClass().addAll(Styles.TEXT_BOLD, Styles.SMALL);
            name.setWrapText(true);
            Label desc = new Label(b.description);
            desc.getStyleClass().addAll(Styles.TEXT_SUBTLE, Styles.SMALL);
            desc.setWrapText(true);
            desc.setMaxWidth(140);

            card.getChildren().addAll(icon, name, desc);
            badgesContainer.getChildren().add(card);
        }
    }

    /** Demo badges (replace with user achievements from backend). */
    private java.util.List<BadgeDisplay> getBadgesForUser() {
        java.util.List<BadgeDisplay> list = new java.util.ArrayList<>();
        if (user == null) return list;
        list.add(new BadgeDisplay("\uD83C\uDFC6", "First Steps", "Complete your first lesson"));
        list.add(new BadgeDisplay("🔥", "Streak Master", "7-day coding streak"));
        list.add(new BadgeDisplay("\uD83E\uDDE9", "Code Explorer", "Finish 5 chapters"));
        list.add(new BadgeDisplay("⭐", "Rising Star", "Reach Level 3"));
        return list;
    }

    private static final class BadgeDisplay {
        final String icon;
        final String name;
        final String description;

        BadgeDisplay(String icon, String name, String description) {
            this.icon = icon;
            this.name = name;
            this.description = description;
        }
    }
}

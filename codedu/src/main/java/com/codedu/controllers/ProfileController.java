package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.gamification.Achievement;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;

<<<<<<< Updated upstream
import java.util.List;
=======
import javafx.event.EventHandler;
>>>>>>> Stashed changes
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
<<<<<<< Updated upstream
import javafx.scene.layout.HBox;
=======
import javafx.scene.layout.StackPane;
>>>>>>> Stashed changes
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.springframework.stereotype.Controller;

<<<<<<< Updated upstream
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
=======
import java.util.List;
import java.util.function.Consumer;

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
    @FXML private VBox avatarCard; // UPGRADED: The Top Box
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

    private User currentUser;
    private User profileUser;
    private UserGameState gameState;
    private boolean viewingSelf;
    private Consumer<User> onProfileClick;


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
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    private void bindStats() {
        // 1. Initial Null Check for User
        if (user == null) {
            return;
        }
=======
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        chatWindowManager.connectUser(currentUser);
    }

    public void setOnProfileClick(Consumer<User> onProfileClick) {
        this.onProfileClick = onProfileClick;
    }

    private void bindUI() {
        if (profileUser == null) return;
>>>>>>> Stashed changes

        // 2. Username and Avatar Display
        String username = (user.getUsername() != null && !user.getUsername().isEmpty())
                ? user.getUsername() : "User";
        String initial = username.substring(0, 1).toUpperCase();

        avatarDisplay.setText(initial);
        avatarDisplay.setAlignment(Pos.CENTER);
<<<<<<< Updated upstream
        avatarDisplay.setMinSize(80, 80);
        avatarDisplay.setPrefSize(80, 80);
        avatarDisplay.setMaxSize(80, 80);
        avatarDisplay.setShape(new Circle(40));
        avatarDisplay.getStyleClass().add(Styles.TITLE_2);

        usernameDisplay.setText(username);
        usernameDisplay.getStyleClass().add(Styles.TITLE_3);
=======
        avatarDisplay.setMinSize(100, 100);
        avatarDisplay.setShape(new Circle(50));
        avatarDisplay.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.15); -fx-border-width: 2; -fx-border-radius: 50;");
        
        usernameDisplay.setText(username);
        usernameDisplay.setStyle("-fx-text-fill: #D35400; -fx-font-weight: bold; -fx-font-size: 26px;");

        profileXpBar.setStyle(
            "-fx-accent: #D35400; " + 
            "-fx-control-inner-background: #1E272E; " + // Darker track for better contrast
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
            targetXp = level * 1000;
            tokens = gameState.getTokenBalance();
        }
>>>>>>> Stashed changes

        // 3. Level and XP Logic (Using the controller's gameState field)
        int level = (gameState != null) ? gameState.getLevel() : 1;
        badgeDisplay.setText("Level " + level);
<<<<<<< Updated upstream

        int xp = (gameState != null) ? gameState.getXp() : 0;
        int levelCap = Math.max(1, level * 1000);
        double progress = Math.max(0, Math.min(1, (double) xp / levelCap));

        profileXpBar.setProgress(progress);
        profileXpLabel.setText(xp + " / " + levelCap + " XP");

        // 4. Token Balance Logic (FIX: Avoiding the NPE by checking gameState)
        int tokens = (gameState != null) ? gameState.getTokenBalance() : 0;
        profileTokenLabel.setText(String.valueOf(tokens));

        // 5. Inventory Items Logic
        int itemCount = 0;
        if (user.getInventory() != null && user.getInventory().getItems() != null) {
            for (InventoryItem inv : user.getInventory().getItems()) {
                itemCount += Math.max(0, inv.getQuantity());
            }
        }
        profileItemsLabel.setText(String.valueOf(itemCount));

        // 6. "Add Friend" Button Visibility Logic
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

        // 7. Sections Visibility (Badges, Friends, Avatars)
        if (badgesSection != null) {
            badgesSection.setVisible(true);
            badgesSection.setManaged(true);
            if (badgesSectionTitle != null) badgesSectionTitle.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);
            if (noBadgesLabel != null) noBadgesLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
            buildBadgesList();
        }

        if (friendsSection != null) {
            friendsSection.setVisible(viewingSelf);
            friendsSection.setManaged(viewingSelf);
            if (viewingSelf) {
                if (friendsSectionTitle != null) friendsSectionTitle.getStyleClass().add(Styles.TITLE_3);
                buildFriendsList();
            }
        }

=======
        
        targetXp = level * 1000;
        double progress = targetXp == 0 ? 0 : Math.min(1.0, (double) xp / targetXp);
        profileXpBar.setProgress(progress);
        profileXpLabel.setText(xp + " / " + targetXp + " XP");
        
        profileTokenLabel.setText(String.valueOf(tokens));
        profileTokenLabel.setStyle("-fx-text-fill: #F1C40F;");

        if (profileUser != null && profileUser.getInventory() != null && profileUser.getInventory().getItems() != null) {
            items = profileUser.getInventory().getItems().size();
        }
        profileItemsLabel.setText(String.valueOf(items));
    }

    private void bindBadgesSection() {
        if (badgesSection == null || badgesContainer == null) return;

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
        friendshipUIManager.renderFriendsList(friendsList, noFriendsLabel, currentUser, viewingSelf,
                new Runnable() {
                    @Override public void run() { bindFriendsSection(); }
                }, onProfileClick);
    }

    private void bindAvatarsSection() {
>>>>>>> Stashed changes
        if (avatarsSection != null) {
            avatarsSection.setVisible(viewingSelf);
            avatarsSection.setManaged(viewingSelf);
        }

        // 8. Card Styling (Ensures UI consistency)
        applyCardStyles();
    }

    /** * Helper to keep bindStats clean: applies AtlantaFX styles to the VBox cards
     */
    private void applyCardStyles() {
<<<<<<< Updated upstream
        if (avatarCard != null) avatarCard.getStyleClass().setAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE, Styles.ELEVATED_1);
        if (xpCard != null) xpCard.getStyleClass().setAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        if (tokensCard != null) tokensCard.getStyleClass().setAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        if (itemsCard != null) itemsCard.getStyleClass().setAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
=======

        String smoothRadius = "22"; 
    String softerOrange = "#D35400";
    
    if (avatarCard != null) {
        avatarCard.setStyle(
            "-fx-background-color: #1A1E23; " + 
            "-fx-background-radius: " + smoothRadius + "; " +
            "-fx-border-color: " + softerOrange + "; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: " + smoothRadius + "; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);" 
        ); 
    }
    
    final VBox[] dashboardCards = {xpCard, tokensCard, itemsCard};
    for (final VBox card : dashboardCards) {
        if (card == null) continue;
        
        card.setStyle(
            "-fx-background-color: #1E2329; " +
            "-fx-border-color: #34495E; " + 
            "-fx-border-width: 1.2; " +
            "-fx-background-radius: " + smoothRadius + "; " +
            "-fx-border-radius: " + smoothRadius + ";"
        );
        
        card.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                card.setStyle(
                    "-fx-background-color: #242B33; " +
                    "-fx-border-color: " + softerOrange + "; " + 
                    "-fx-border-width: 1.2; " +
                    "-fx-background-radius: " + smoothRadius + "; " +
                    "-fx-border-radius: " + smoothRadius + ";"
                );
            }
        });

        card.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                card.setStyle(
                    "-fx-background-color: #1E2329; " +
                    "-fx-border-color: #34495E; " + 
                    "-fx-border-width: 1.2; " +
                    "-fx-background-radius: " + smoothRadius + "; " +
                    "-fx-border-radius: " + smoothRadius + ";"
                );
            }
        });
    }
>>>>>>> Stashed changes
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

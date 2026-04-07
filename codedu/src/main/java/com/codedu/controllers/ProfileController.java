package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.gamification.Achievement;
import com.codedu.models.gamification.Badge;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.services.interfaces.ChatWindowManager;
import com.codedu.services.interfaces.FriendshipUIManager;
import com.codedu.services.interfaces.InventoryItemService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Controller
public class ProfileController {

    private final String LOGO_BLUE = "#00AEEF";
    private final String BG_CARD = "#1A1E23";
    private final String BORDER_COLOR = "#34495E";

    @Autowired
    private ChatWindowManager chatWindowManager;
    @Autowired
    private FriendshipUIManager friendshipUIManager;
    @Autowired
    private InventoryItemService inventoryItemService;

    @FXML private Label avatarDisplay, usernameDisplay, badgeDisplay, profileXpLabel, profileItemsLabel, noAvatarsLabel, friendsSectionTitle, noFriendsLabel, badgesSectionTitle, noBadgesLabel;
    @FXML private ProgressBar profileXpBar;
    @FXML private VBox avatarsSection, avatarCard, xpCard, itemsCard, friendsSection, friendsList, badgesSection;
    @FXML private Button addFriendButton;
    @FXML private FlowPane avatarGrid, badgesContainer;

    private User currentUser, profileUser;
    private UserGameState gameState;
    private boolean viewingSelf;
    private Consumer<User> onProfileClick;
    private Runnable onNavigateToInventory;

    public void setOnNavigateToInventory(Runnable callback) { this.onNavigateToInventory = callback; }
    public void setViewingSelf(boolean viewingSelf) { this.viewingSelf = viewingSelf; }

    public void setUserModel(User user) {
        this.profileUser = user;
        if (viewingSelf) this.currentUser = user;
        bindUI();
    }

    public void setGameState(UserGameState gameState) {
        this.gameState = gameState;
        if (profileUser != null) {
            bindStats();
            bindBadgesSection();
        }
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        chatWindowManager.connectUser(currentUser);
    }

    public void setOnProfileClick(Consumer<User> onProfileClick) { this.onProfileClick = onProfileClick; }

    public void setCompetitor(Competitor competitor, List<Competitor> competitorOrder, User hydratedProfileUser) {
        this.viewingSelf = false;
        this.profileUser = (hydratedProfileUser != null) ? hydratedProfileUser : (competitor != null ? competitor.getUser() : null);
        if (profileUser != null) this.gameState = profileUser.getGameState();
        if (this.gameState == null && profileUser != null) this.gameState = UserGameState.newDefault();
        bindUI();
    }

    private void bindUI() {
        if (profileUser == null) return;

        String username = profileUser.getDisplayName();
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();

        avatarDisplay.setAlignment(Pos.CENTER);
        avatarDisplay.setPadding(javafx.geometry.Insets.EMPTY);
        avatarDisplay.setMinSize(120, 120);
        avatarDisplay.setPrefSize(120, 120);

        Optional<InventoryItem> equippedAvatar = Optional.empty();
        try { equippedAvatar = inventoryItemService.getEquippedAvatar(profileUser); } catch (Exception ignored) {}

        if (equippedAvatar.isPresent()) {
            setupAvatarImage(equippedAvatar.get(), initial);
        } else {
            setupAvatarText(initial);
        }

        usernameDisplay.setText("@" + username);
        usernameDisplay.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 30px;");

        profileXpBar.getStyleClass().add(Styles.MEDIUM);
        profileXpBar.setStyle("-fx-accent: " + LOGO_BLUE + "; -fx-control-inner-background: #2e3440; -fx-background-radius: 20; -fx-border-radius: 20;");
        profileXpBar.skinProperty().addListener((obs, old, newSkin) -> {
            if (newSkin != null) {
                Region track = (Region) profileXpBar.lookup(".track");
                Region bar = (Region) profileXpBar.lookup(".bar");
                if (track != null) track.setStyle("-fx-background-radius: 20;");
                if (bar != null) bar.setStyle("-fx-background-radius: 20;");
            }
        });

        bindStats();
        bindBadgesSection();
        bindAddFriendButton();
        bindFriendsSection();
        bindAvatarsSection();
        applyCardStyles();
    }

    private void setupAvatarImage(InventoryItem item, String initial) {
        String itemName = item.getItem().getName().toLowerCase();
        String imageFile = itemName.contains("ninja") ? "avatar_ninja.png" : itemName.contains("wizard") ? "avatar_wizard.png" : "avatar_basic.png";
        try {
            Image img = new Image(getClass().getResource("/com/codedu/images/avatars/" + imageFile).toExternalForm());
            ImageView iv = new ImageView(img);
            iv.setFitWidth(110);
            iv.setFitHeight(110);
            iv.setClip(new Circle(55, 55, 55));
            avatarDisplay.setGraphic(iv);
            avatarDisplay.setText("");
            avatarDisplay.setStyle("-fx-border-color: " + LOGO_BLUE + "; -fx-border-width: 4; -fx-border-radius: 70;");
        } catch (Exception e) {
            setupAvatarText(initial);
        }
    }

    private void setupAvatarText(String initial) {
        avatarDisplay.setGraphic(null);
        avatarDisplay.setText(initial);
        avatarDisplay.setStyle("-fx-font-size: 45px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + LOGO_BLUE + "; -fx-background-radius: 70; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 4; -fx-border-radius: 70;");
    }

    private void bindStats() {
        int level = (gameState != null) ? gameState.getLevel() : 1;
        int xp = (gameState != null) ? gameState.getXp() : 0;

        int levelCap = level * 100;

        badgeDisplay.setText("\uD83C\uDFC6 LVL " + level);
        badgeDisplay.setStyle("-fx-text-fill: " + LOGO_BLUE + "; -fx-font-weight: bold; -fx-font-size: 18px;");

        profileXpBar.setProgress(Math.min(1.0, (double) xp / levelCap));
        profileXpLabel.setText(xp + " / " + levelCap + " XP");

        int items = (profileUser != null && profileUser.getInventory() != null) ? profileUser.getInventory().getItems().size() : 0;
        profileItemsLabel.setText(String.valueOf(items));
    }

    private void bindBadgesSection() {
        if (badgesContainer == null) {
            return;
        }
        badgesContainer.getChildren().clear();
        List<Achievement> earned = (gameState != null) ? gameState.getAchievements() : null;

        if (earned == null || earned.isEmpty()) {
            noBadgesLabel.setManaged(true);
            noBadgesLabel.setVisible(true);
            return;
        }
        noBadgesLabel.setManaged(false);
        noBadgesLabel.setVisible(false);

        earned.stream()
                .sorted(Comparator.comparing(Achievement::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(a -> badgesContainer.getChildren().add(buildEarnedBadgeCard(a)));
    }

    private VBox buildEarnedBadgeCard(Achievement achievement) {
        VBox badgePod = new VBox(6);
        badgePod.setAlignment(Pos.TOP_CENTER);
        badgePod.setPrefWidth(120);
        badgePod.setMaxWidth(120);
        badgePod.setStyle("-fx-background-color: #242B33; -fx-background-radius: 15; -fx-border-color: " + LOGO_BLUE
                + "44; -fx-border-radius: 15; -fx-padding: 10;");

        Node graphic = createBadgeGraphic(achievement);
        badgePod.getChildren().add(graphic);

        String title = badgeTitle(achievement);
        Label name = new Label(title);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);
        name.setMaxWidth(110);

        String desc = badgeDescription(achievement);
        if (!desc.isBlank()) {
            Label body = new Label(desc);
            body.setWrapText(true);
            body.setAlignment(Pos.CENTER);
            body.setMaxWidth(110);
            body.setStyle("-fx-text-fill: #8E96A0; -fx-font-size: 9px;");
            badgePod.getChildren().addAll(name, body);
        } else {
            badgePod.getChildren().add(name);
        }
        return badgePod;
    }

    private static String badgeTitle(Achievement a) {
        Badge b = a.getBadge();
        if (b != null && b.getTitle() != null && !b.getTitle().isBlank()) {
            return b.getTitle();
        }
        return a.getName() != null ? a.getName() : "Achievement";
    }

    private static String badgeDescription(Achievement a) {
        Badge b = a.getBadge();
        if (b != null && b.getDescription() != null && !b.getDescription().isBlank()) {
            return b.getDescription();
        }
        return a.getCriteria() != null ? a.getCriteria() : "";
    }

    private Node createBadgeGraphic(Achievement achievement) {
        final int size = 56;
        Image img = loadBadgeImage(achievement, size);
        if (img != null && !img.isError()) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        }
        Label fallback = new Label(emojiForAchievement(achievement.getName()));
        fallback.setStyle("-fx-font-size: 28px;");
        fallback.setAlignment(Pos.CENTER);
        fallback.setMinSize(size, size);
        return fallback;
    }

    private Image loadBadgeImage(Achievement achievement, int size) {
        Badge b = achievement.getBadge();
        if (b == null || b.getIconURL() == null || b.getIconURL().isBlank()) {
            return null;
        }
        String raw = b.getIconURL().trim();
        java.net.URL url = getClass().getResource(raw);
        if (url == null && raw.startsWith("/assets/badges/")) {
            String file = raw.substring(raw.lastIndexOf('/') + 1);
            url = getClass().getResource("/com/codedu/images/badges/" + file);
        }
        if (url == null && raw.startsWith("/assets/")) {
            int slash = raw.lastIndexOf('/');
            if (slash >= 0 && slash < raw.length() - 1) {
                url = getClass().getResource("/com/codedu/images/badges/" + raw.substring(slash + 1));
            }
        }
        if (url == null) {
            return null;
        }
        return new Image(url.toExternalForm(), size, size, true, true, false);
    }

    private static String emojiForAchievement(String achievementName) {
        if (achievementName == null) {
            return "\uD83C\uDFC6";
        }
        return switch (achievementName) {
            case "First Blood" -> "\u2694\uFE0F";
            case "Veteran" -> "\uD83C\uDF96\uFE0F";
            case "Gladiator" -> "\uD83D\uDEE1\uFE0F";
            case "Scholar" -> "\uD83D\uDCDA";
            case "Professor" -> "\uD83C\uDF93";
            case "Rich" -> "\uD83D\uDCB0";
            case "Tycoon" -> "\uD83D\uDCB5";
            case "Dedicated" -> "\u2B50";
            case "Master" -> "\uD83C\uDFC6";
            case "Social Butterfly" -> "\uD83E\uDD1D";
            default -> "\uD83C\uDFC6";
        };
    }

    private void applyCardStyles() {
        String radius = "22";
        avatarCard.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: " + radius + "; -fx-border-color: " + LOGO_BLUE + "; -fx-border-width: 2; -fx-border-radius: " + radius + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 8);");

        VBox[] statsCards = { xpCard, itemsCard };
        for (VBox card : statsCards) {
            if (card == null) continue;
            card.setStyle("-fx-background-color: #1E2329; -fx-border-color: " + BORDER_COLOR + "; -fx-background-radius: " + radius + "; -fx-border-radius: " + radius + ";");
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #242B33; -fx-border-color: " + LOGO_BLUE + "; -fx-background-radius: " + radius + "; -fx-border-radius: " + radius + ";"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2329; -fx-border-color: " + BORDER_COLOR + "; -fx-background-radius: " + radius + "; -fx-border-radius: " + radius + ";"));
        }

        if (itemsCard != null && onNavigateToInventory != null) {
            itemsCard.setOnMouseClicked(e -> onNavigateToInventory.run());
        }
    }

    private void bindAddFriendButton() { friendshipUIManager.setupAddFriendButton(addFriendButton, currentUser, profileUser, viewingSelf); }
    private void bindFriendsSection() {
        if (friendsSection != null) {
            friendsSection.setVisible(viewingSelf);
            friendsSection.setManaged(viewingSelf);
        }
        friendshipUIManager.renderFriendsList(friendsList, noFriendsLabel, currentUser, viewingSelf, this::bindFriendsSection, onProfileClick);
    }
    private void bindAvatarsSection() { if (avatarsSection != null) { avatarsSection.setVisible(viewingSelf); avatarsSection.setManaged(viewingSelf); } }
}
package com.codedu.controllers;

import com.codedu.models.StoreItem;
import com.codedu.models.StoreItem.Category;
import com.codedu.models.UserModel;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Store view.
 * Displays purchasable items in a card grid, grouped by category.
 */
public class StoreController {

    @FXML
    private VBox storeContent;
    @FXML
    private Label storeTokenLabel;

    private UserModel user;
    private final List<StoreItem> allItems = new ArrayList<>();

    public void setUserModel(UserModel user) {
        this.user = user;
        storeTokenLabel.setText("\uD83E\uDE99 " + user.getTokenBalance() + " Tokens");
        user.tokenBalanceProperty()
                .addListener((obs, o, n) -> storeTokenLabel.setText("\uD83E\uDE99 " + n.intValue() + " Tokens"));
        loadMockItems();
        buildGrid();
    }

    private void loadMockItems() {
        // Avatars
        allItems.add(new StoreItem("Ninja Coder", "A stealthy coding warrior", "\uD83E\uDD77", 200, Category.AVATAR));
        allItems.add(new StoreItem("Robot Dev", "Automated perfection", "\uD83E\uDD16", 300, Category.AVATAR));
        allItems.add(new StoreItem("Wizard Hacker", "Magic meets code", "\uD83E\uDDD9", 250, Category.AVATAR));
        allItems.add(new StoreItem("Astronaut", "Code among the stars", "\uD83D\uDE80", 350, Category.AVATAR));
        allItems.add(new StoreItem("Dragon Master", "Legendary beast tamer", "\uD83D\uDC09", 500, Category.AVATAR));

        // Themes
        allItems.add(new StoreItem("Neon Synthwave", "Retro-futuristic vibes", "\uD83C\uDF03", 400, Category.THEME));
        allItems.add(new StoreItem("Ocean Breeze", "Cool blue gradients", "\uD83C\uDF0A", 300, Category.THEME));
        allItems.add(new StoreItem("Forest Night", "Deep emerald darkness", "\uD83C\uDF32", 350, Category.THEME));

        // Power-ups
        allItems.add(new StoreItem("Double XP (1h)", "Earn double XP for 1 hour", "⚡", 150, Category.POWER_UP));
        allItems.add(new StoreItem("Hint Token", "Get a free hint on any challenge", "\uD83D\uDCA1", 100,
                Category.POWER_UP));
        allItems.add(new StoreItem("Streak Shield", "Protect your streak for one day", "\uD83D\uDEE1\uFE0F", 200,
                Category.POWER_UP));

        // Bundles
        allItems.add(new StoreItem("Starter Pack", "3 avatars + Double XP + 500 bonus tokens", "\uD83C\uDF81", 800,
                Category.BUNDLE));
    }

    private void buildGrid() {
        storeContent.getChildren().clear();

        // Group items by category
        Map<Category, List<StoreItem>> grouped = new LinkedHashMap<>();
        grouped.put(Category.AVATAR, new ArrayList<>());
        grouped.put(Category.THEME, new ArrayList<>());
        grouped.put(Category.POWER_UP, new ArrayList<>());
        grouped.put(Category.BUNDLE, new ArrayList<>());
        for (StoreItem item : allItems) {
            grouped.get(item.getCategory()).add(item);
        }

        for (Map.Entry<Category, List<StoreItem>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty())
                continue;

            Label categoryLabel = new Label(categoryTitle(entry.getKey()));
            categoryLabel.getStyleClass().add("store-category-title");
            storeContent.getChildren().add(categoryLabel);

            FlowPane grid = new FlowPane(16, 16);
            grid.setAlignment(Pos.TOP_LEFT);
            for (StoreItem item : entry.getValue()) {
                grid.getChildren().add(buildItemCard(item));
            }
            storeContent.getChildren().add(grid);
        }
    }

    private VBox buildItemCard(StoreItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("store-card");
        card.setPadding(new Insets(20, 20, 16, 20));
        card.setPrefWidth(200);
        card.setMinWidth(200);
        card.setMaxWidth(200);
        card.setAlignment(Pos.TOP_CENTER);

        Label emoji = new Label(item.getEmoji());
        emoji.setStyle("-fx-font-size: 40px;");

        Label name = new Label(item.getName());
        name.getStyleClass().add("store-item-name");
        name.setWrapText(true);

        Label desc = new Label(item.getDescription());
        desc.getStyleClass().add("store-item-desc");
        desc.setWrapText(true);
        desc.setMaxHeight(36);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox priceRow = new HBox(8);
        priceRow.setAlignment(Pos.CENTER);
        Label priceLabel = new Label("\uD83E\uDE99 " + item.getPrice());
        priceLabel.getStyleClass().add("store-price-tag");
        priceRow.getChildren().add(priceLabel);

        Button buyBtn = new Button(item.isOwned() ? "✓ Owned" : "Buy");
        buyBtn.getStyleClass().add(item.isOwned() ? "store-owned-btn" : "store-buy-btn");
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        if (!item.isOwned()) {
            buyBtn.setOnAction(e -> {
                if (user.buyItem(item)) {
                    buyBtn.setText("✓ Owned");
                    buyBtn.getStyleClass().removeAll("store-buy-btn");
                    buyBtn.getStyleClass().add("store-owned-btn");
                    buyBtn.setOnAction(null);

                    // Purchase animation
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                    st.setToX(1.08);
                    st.setToY(1.08);
                    st.setAutoReverse(true);
                    st.setCycleCount(2);
                    st.play();
                }
            });
        }

        // Hover animation
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.04);
            st.setToY(1.04);
            st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        card.getChildren().addAll(emoji, name, desc, spacer, priceRow, buyBtn);
        return card;
    }

    private String categoryTitle(Category c) {
        return switch (c) {
            case AVATAR -> "\uD83D\uDC64  Avatars";
            case THEME -> "\uD83C\uDFA8  Themes";
            case POWER_UP -> "⚡  Power-Ups";
            case BUNDLE -> "\uD83C\uDF81  Bundles";
        };
    }
}

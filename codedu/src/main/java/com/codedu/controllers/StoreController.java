package com.codedu.controllers;

import com.codedu.models.Item;
import com.codedu.models.ItemType;
import com.codedu.models.User;
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

    private User user;
    private final List<Item> allItems = new ArrayList<>();

    public void setUserModel(User user) {
        this.user = user;
        storeTokenLabel.setText("\uD83E\uDE99 " + user.getTokenBalance() + " Tokens");
        loadMockItems();
        buildGrid();
    }

    private void loadMockItems() {
        // Avatars
        allItems.add(new Item("Ninja Coder", "A stealthy coding warrior", "\uD83E\uDD77", 200, ItemType.AVATAR));
        allItems.add(new Item("Robot Dev", "Automated perfection", "\uD83E\uDD16", 300, ItemType.AVATAR));
        allItems.add(new Item("Wizard Hacker", "Magic meets code", "\uD83E\uDDD9", 250, ItemType.AVATAR));
        allItems.add(new Item("Astronaut", "Code among the stars", "\uD83D\uDE80", 350, ItemType.AVATAR));
        allItems.add(new Item("Dragon Master", "Legendary beast tamer", "\uD83D\uDC09", 500, ItemType.AVATAR));

        // Power-ups
        allItems.add(new Item("Double XP (1h)", "Earn double XP for 1 hour", "\u26A1", 150, ItemType.POWER_UP));
        allItems.add(new Item("Hint Token", "Get a free hint on any challenge", "\uD83D\uDCA1", 100,
                ItemType.POWER_UP));
        allItems.add(new Item("Streak Shield", "Protect your streak for one day", "\uD83D\uDEE1\uFE0F", 200,
                ItemType.POWER_UP));

        // AI Usage
        allItems.add(new Item("AI Code Review", "Get AI-powered feedback on your code", "\uD83E\uDD16", 120,
                ItemType.AI_USAGE));
        allItems.add(new Item("AI Debugging Assistant", "Let AI help you find and fix bugs", "\uD83D\uDC1B", 150,
                ItemType.AI_USAGE));
        allItems.add(new Item("AI Solution Explainer", "Get step-by-step explanations from AI", "\uD83D\uDCD6", 100,
                ItemType.AI_USAGE));
    }

    private void buildGrid() {
        storeContent.getChildren().clear();

        // Group items by type
        Map<ItemType, List<Item>> grouped = new LinkedHashMap<>();
        grouped.put(ItemType.AVATAR, new ArrayList<>());
        grouped.put(ItemType.POWER_UP, new ArrayList<>());
        grouped.put(ItemType.AI_USAGE, new ArrayList<>());
        for (Item item : allItems) {
            grouped.get(item.getType()).add(item);
        }

        for (Map.Entry<ItemType, List<Item>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty())
                continue;

            Label categoryLabel = new Label(categoryTitle(entry.getKey()));
            categoryLabel.getStyleClass().add("store-category-title");
            storeContent.getChildren().add(categoryLabel);

            FlowPane grid = new FlowPane(16, 16);
            grid.setAlignment(Pos.TOP_LEFT);
            for (Item item : entry.getValue()) {
                grid.getChildren().add(buildItemCard(item));
            }
            storeContent.getChildren().add(grid);
        }
    }

    private VBox buildItemCard(Item item) {
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
                if (user.getTokenBalance() >= item.getPrice()) {
                    user.setTokenBalance(user.getTokenBalance() - item.getPrice());
                    item.setOwned(true);
                    buyBtn.setText("✓ Owned");
                    buyBtn.getStyleClass().removeAll("store-buy-btn");
                    buyBtn.getStyleClass().add("store-owned-btn");
                    buyBtn.setOnAction(null);
                    storeTokenLabel.setText("\uD83E\uDE99 " + user.getTokenBalance() + " Tokens");

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

    private String categoryTitle(ItemType c) {
        return switch (c) {
            case AVATAR -> "\uD83D\uDC64  Avatars";
            case POWER_UP -> "\u26A1  Power-Ups";
            case AI_USAGE -> "\uD83E\uDD16  AI Usage";
            default -> c.name();
        };
    }
}

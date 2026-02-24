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
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Store view.
 * Displays purchasable items in a card grid, grouped by category.
 */
@Controller
public class StoreController {

    @FXML
    private VBox storeContent;
    @FXML
    private Label storeTokenLabel;

    private User user;
    private final List<Item> allItems = new ArrayList<>();

    public void setUserModel(User user) {
        this.user = user;
        storeTokenLabel.setText("Tokens: " + user.getTokenBalance());
        loadMockItems();
        buildGrid();
    }

    private void loadMockItems() {
        // Avatars
        allItems.add(new Item("Ninja Coder", "A stealthy coding warrior", "", 200, ItemType.AVATAR));
        allItems.add(new Item("Robot Dev", "Automated perfection", "", 300, ItemType.AVATAR));
        allItems.add(new Item("Wizard Hacker", "Magic meets code", "", 250, ItemType.AVATAR));
        allItems.add(new Item("Astronaut", "Code among the stars", "", 350, ItemType.AVATAR));
        allItems.add(new Item("Dragon Master", "Legendary beast tamer", "", 500, ItemType.AVATAR));

        // Power-ups
        allItems.add(new Item("Double XP (1h)", "Earn double XP for 1 hour", "", 150, ItemType.POWER_UP));
        allItems.add(new Item("Hint Token", "Get a free hint on any challenge", "", 100,
                ItemType.POWER_UP));
        allItems.add(new Item("Streak Shield", "Protect your streak for one day", "", 200,
                ItemType.POWER_UP));

        // AI Usage
        allItems.add(new Item("AI Code Review", "Get AI-powered feedback on your code", "", 120,
                ItemType.AI_USAGE));
        allItems.add(new Item("AI Debugging Assistant", "Let AI help you find and fix bugs", "", 150,
                ItemType.AI_USAGE));
        allItems.add(new Item("AI Solution Explainer", "Get step-by-step explanations from AI", "", 100,
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

        String iconText = item.getName().isEmpty() ? "" : item.getName().substring(0, 1).toUpperCase();
        Label emoji = new Label(iconText);
        emoji.setStyle("-fx-font-size: 32px; -fx-text-fill: #e0e0e0;");

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
        Label priceLabel = new Label("Tokens: " + item.getPrice());
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
                    buyBtn.setText("Owned");
                    buyBtn.getStyleClass().removeAll("store-buy-btn");
                    buyBtn.getStyleClass().add("store-owned-btn");
                    buyBtn.setOnAction(null);
                    storeTokenLabel.setText("Tokens: " + user.getTokenBalance());

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
            case AVATAR -> "Avatars";
            case POWER_UP -> "Power-ups";
            case AI_USAGE -> "AI features";
            default -> c.name();
        };
    }
}

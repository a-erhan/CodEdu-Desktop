package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.ItemService;
import com.codedu.services.interfaces.UserService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaFX controller for the catalog loaded from {@link ItemService} (database).
 * Falls back to the same mock list as the legacy store when the {@code items} table is empty.
 */
@Controller
public class ItemController {

    @FXML
    private VBox itemContent;
    @FXML
    private Label itemTokenLabel;

    private User user;
    private final List<Item> allItems = new ArrayList<>();

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    public void setUserModel(User user) {
        if (user == null)
            return;
        this.user = user;
        if (user.getGameState() != null) {
            itemTokenLabel.setText("Tokens: " + user.getGameState().getTokenBalance());
        }
        loadCatalogItems();
        markOwnedItemsFromInventory();
        buildGrid();
    }

    private void loadCatalogItems() {
        allItems.clear();
        List<Item> fromDb = itemService.getAllItems();
        if (!fromDb.isEmpty()) {
            allItems.addAll(fromDb);
        } else {
            addLegacyMockItems();
        }
    }

    private void addLegacyMockItems() {
        allItems.add(new Item("Ninja Coder", "A stealthy coding warrior", "", 200, ItemType.AVATAR));
        allItems.add(new Item("Robot Dev", "Automated perfection", "", 300, ItemType.AVATAR));
        allItems.add(new Item("Wizard Hacker", "Magic meets code", "", 250, ItemType.AVATAR));
        allItems.add(new Item("Astronaut", "Code among the stars", "", 350, ItemType.AVATAR));
        allItems.add(new Item("Dragon Master", "Legendary beast tamer", "", 500, ItemType.AVATAR));
        allItems.add(new Item("Double XP (1h)", "Earn double XP for 1 hour", "", 150, ItemType.BOOSTER));
        allItems.add(new Item("Hint Token", "Get a free hint on any challenge", "", 100, ItemType.BOOSTER));
        allItems.add(new Item("Streak Shield", "Protect your streak for one day", "", 200, ItemType.BOOSTER));
        allItems.add(new Item("AI Usage", "Get AI-powered feedback on your code", "", 120, ItemType.AI_USAGE));
    }

    private void markOwnedItemsFromInventory() {
        if (user.getInventory() == null || user.getInventory().getItems() == null) {
            return;
        }
        for (InventoryItem inv : user.getInventory().getItems()) {
            Item invItem = inv.getItem();
            if (invItem == null || invItem.getName() == null)
                continue;
            for (Item catalogItem : allItems) {
                if (catalogItem.getName() != null &&
                        catalogItem.getName().equalsIgnoreCase(invItem.getName())) {
                    catalogItem.setOwned(true);
                }
            }
        }
    }

    private void buildGrid() {
        itemContent.getChildren().clear();

        if (allItems.isEmpty()) {
            Label empty = new Label("No items in the catalog.");
            empty.getStyleClass().add(Styles.TEXT_MUTED);
            itemContent.getChildren().add(empty);
            return;
        }

        Map<ItemType, List<Item>> grouped = new LinkedHashMap<>();
        grouped.put(ItemType.AVATAR, new ArrayList<>());
        grouped.put(ItemType.BOOSTER, new ArrayList<>());
        grouped.put(ItemType.AI_USAGE, new ArrayList<>());
        for (Item item : allItems) {
            grouped.get(item.getType()).add(item);
        }

        for (Map.Entry<ItemType, List<Item>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty())
                continue;

            Label categoryLabel = new Label(categoryTitle(entry.getKey()));
            categoryLabel.getStyleClass().add(Styles.TITLE_3);
            itemContent.getChildren().add(categoryLabel);

            FlowPane grid = new FlowPane(16, 16);
            grid.setAlignment(Pos.TOP_LEFT);
            for (Item item : entry.getValue()) {
                grid.getChildren().add(buildItemCard(item));
            }
            itemContent.getChildren().add(grid);
        }
    }

    private VBox buildItemCard(Item item) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        card.setPadding(new Insets(20, 20, 16, 20));
        card.setPrefWidth(200);
        card.setMinWidth(200);
        card.setMaxWidth(200);
        card.setAlignment(Pos.TOP_CENTER);

        String iconText = item.getName().isEmpty() ? "" : item.getName().substring(0, 1).toUpperCase();
        Label emoji = new Label(iconText);

        Label name = new Label(item.getName());
        name.getStyleClass().add(Styles.TEXT_BOLD);
        name.setWrapText(true);

        Label desc = new Label(item.getDescription());
        desc.getStyleClass().add(Styles.TEXT_SUBTLE);
        desc.setWrapText(true);
        desc.setMaxHeight(36);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox priceRow = new HBox(8);
        priceRow.setAlignment(Pos.CENTER);
        Label priceLabel = new Label("Tokens: " + item.getPrice());
        priceLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        priceRow.getChildren().add(priceLabel);

        Button buyBtn = new Button(item.isOwned() ? "Owned" : "Buy");
        if (item.isOwned()) {
            buyBtn.getStyleClass().addAll(Styles.SUCCESS, Styles.ROUNDED, Styles.FLAT);
        } else {
            buyBtn.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
        }
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        if (!item.isOwned()) {
            buyBtn.setOnAction(e -> {
                if (user != null &&
                        user.getGameState() != null &&
                        user.getGameState().hasEnoughTokens(item.getPrice())) {

                    user.getGameState().setTokenBalance(
                            user.getGameState().getTokenBalance() - item.getPrice());

                    if (user.getInventory() == null) {
                        user.setInventory(new com.codedu.models.user.UserInventory());
                    }
                    InventoryItem invItem = InventoryItem.builder()
                            .item(item)
                            .quantity(1)
                            .inventory(user.getInventory())
                            .build();
                    user.getInventory().addItem(invItem);

                    userService.saveUser(user);

                    item.setOwned(true);
                    buyBtn.setText("Owned");
                    buyBtn.getStyleClass().clear();
                    buyBtn.getStyleClass().addAll(Styles.SUCCESS, Styles.ROUNDED, Styles.FLAT);
                    buyBtn.setOnAction(null);

                    itemTokenLabel.setText("Tokens: " + user.getGameState().getTokenBalance());

                    ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                    st.setToX(1.08);
                    st.setToY(1.08);
                    st.setAutoReverse(true);
                    st.setCycleCount(2);
                    st.play();
                }
            });
        }

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
            case BOOSTER -> "Power-ups";
            case AI_USAGE -> "AI features";
            default -> c.name();
        };
    }
}

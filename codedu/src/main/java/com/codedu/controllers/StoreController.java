package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.InventoryItemService;
import com.codedu.services.interfaces.StoreService;
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
import java.util.function.Consumer;

/**
 * Controller for the Store view.
 * Displays purchasable items in a card grid, grouped by category.
 */
@Controller
public class StoreController {

    @FXML
    private VBox storeContent;

    private User user;
    private List<Item> allItems = List.of();
    private Consumer<User> onUserUpdated;

    @Autowired
    private StoreService storeService;

    @Autowired
    private InventoryItemService inventoryItemService;

    public void setOnUserUpdated(Consumer<User> onUserUpdated) {
        this.onUserUpdated = onUserUpdated;
    }

    public void setUserModel(User user) {
        if (user == null)
            return;
        // Keep Store and Inventory separate:
        // Store shows a catalog (Store / Items), Inventory shows the user's owned items (InventoryItem).
        // This method only marks catalog items as owned if the user has them in inventory.
        this.user = user;
        allItems = storeService.getCatalogItems();
        markOwnedItemsFromInventory();
        buildGrid();
    }

    private void markOwnedItemsFromInventory() {
        if (user == null || user.getId() <= 0) {
            return;
        }
        for (Item storeItem : allItems) {
            if (storeItem == null || storeItem.getId() <= 0) {
                continue;
            }
            boolean owned = inventoryItemService.findByUserAndItem(user, storeItem).isPresent();
            storeItem.setOwned(owned);
        }
    }

    private void buildGrid() {
        storeContent.getChildren().clear();

        // Group items by type
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

                    // Do purchase inside a transaction (prevents LazyInitializationException)
                    User updated = storeService.purchaseItem(user.getId(), item.getId()).orElse(null);
                    if (updated != null) {
                        user = updated;
                    }

                    item.setOwned(true);
                    buyBtn.setText("Owned");
                    buyBtn.getStyleClass().clear();
                    buyBtn.getStyleClass().addAll(Styles.SUCCESS, Styles.ROUNDED, Styles.FLAT);
                    buyBtn.setOnAction(null);
                    if (onUserUpdated != null) {
                        onUserUpdated.accept(user);
                    }

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
            case BOOSTER -> "Power-ups";
            case AI_USAGE -> "AI features";
            default -> c.name();
        };
    }
}

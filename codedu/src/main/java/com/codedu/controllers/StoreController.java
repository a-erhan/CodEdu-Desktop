package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.services.interfaces.InventoryItemService;
import com.codedu.services.interfaces.ItemService;
import com.codedu.services.interfaces.StoreService;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
@Scope("prototype")
public class StoreController {

    @FXML
    private VBox storeContent;

    private User user;
    private List<Item> allItems = List.of();
    private Consumer<User> onUserUpdated;

    @Autowired
    private StoreService storeService;
    @Autowired
    private ItemService itemService;

    @Autowired
    private InventoryItemService inventoryItemService;

    public void setOnUserUpdated(Consumer<User> onUserUpdated) {
        this.onUserUpdated = onUserUpdated;
    }

    public void setUserModel(User user) {
        if (user == null)
            return;
        this.user = user;
        allItems = itemService.getAllItemEntities();
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

        Map<ItemType, List<Item>> grouped = new LinkedHashMap<>();

        for (Item item : allItems) {
            grouped.computeIfAbsent(item.getType(), k -> new ArrayList<>()).add(item);
        }

        for (Map.Entry<ItemType, List<Item>> entry : grouped.entrySet()) {
            Label categoryLabel = new Label(categoryTitle(entry.getKey()));
            categoryLabel.getStyleClass().add(Styles.TITLE_3);
            categoryLabel.setPadding(new Insets(20, 0, 10, 0));
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

        javafx.scene.Node iconNode = buildItemIcon(item);

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

        // --- LOGIC CHANGE START ---

        // Determine if the item is a one-time purchase
        boolean isOneTimeType = (item.getType() == ItemType.AVATAR);
        boolean showAsOwned = isOneTimeType && item.isOwned();

        Button buyBtn = new Button();

        if (showAsOwned) {
            buyBtn.setText("Owned");
            buyBtn.getStyleClass().addAll(Styles.SUCCESS, Styles.ROUNDED, Styles.FLAT);
            buyBtn.setDisable(true); // Prevent clicking "Owned"
        } else {
            buyBtn.setText("Buy");
            buyBtn.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
        }
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        // Only allow clicking if it's not already an owned Avatar
        if (!showAsOwned) {
            buyBtn.setOnAction(e -> {
                if (user != null &&
                        user.getGameState() != null &&
                        user.getGameState().hasEnoughTokens(item.getPrice())) {

                    User updated = storeService.purchaseItem(user.getId(), item.getId()).orElse(null);
                    if (updated != null) {
                        user = updated;
                    } else {
                        String nm = item.getName() != null ? item.getName().toLowerCase() : "";
                        boolean heartSingle = nm.contains("heart") && !nm.contains("full");
                        UserGameState gs = user.getGameState();
                        boolean heartsFull = heartSingle && gs != null
                                && gs.getHeartCount() >= UserGameState.MAX_HEARTS;
                        Alert alert = new Alert(heartsFull ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
                        alert.setTitle("Purchase did not complete");
                        alert.setHeaderText(null);
                        if (heartsFull) {
                            alert.setContentText("You already have the maximum number of hearts ("
                                    + UserGameState.MAX_HEARTS + "). Single refills are not available.");
                        } else {
                            alert.setContentText("Could not complete the purchase. Check that you have enough tokens.");
                        }
                        alert.showAndWait();
                    }

                    // If it's an avatar, mark as owned and lock the button
                    if (isOneTimeType) {
                        item.setOwned(true);
                        buyBtn.setText("Owned");
                        buyBtn.getStyleClass().clear();
                        buyBtn.getStyleClass().addAll(Styles.SUCCESS, Styles.ROUNDED, Styles.FLAT);
                        buyBtn.setOnAction(null);
                        buyBtn.setDisable(true);
                    }

                    if (updated != null && onUserUpdated != null) {
                        onUserUpdated.accept(user);
                    }

                    // Success Animation
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                    st.setToX(1.08);
                    st.setToY(1.08);
                    st.setAutoReverse(true);
                    st.setCycleCount(2);
                    st.play();
                }
            });
        }

        // --- LOGIC CHANGE END ---

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

        card.getChildren().addAll(iconNode, name, desc, spacer, priceRow, buyBtn);
        return card;
    }

    private javafx.scene.Node buildItemIcon(Item item) {
        if (item.getType() == ItemType.AVATAR) {
            String name = item.getName().toLowerCase();
            String file = name.contains("ninja") ? "avatar_ninja.png"
                    : name.contains("wizard") ? "avatar_wizard.png"
                    : "avatar_basic.png";
            try {
                var url = getClass().getResource("/com/codedu/images/avatars/" + file);
                if (url != null) {
                    Image img = new Image(url.toExternalForm());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(72);
                    iv.setFitHeight(72);
                    iv.setPreserveRatio(true);
                    iv.setClip(new Circle(36, 36, 36));
                    return iv;
                }
            } catch (Exception ignored) {}
        }
        String iconText = item.getType() == ItemType.AI_USAGE ? "🤖"
                : item.getType() == ItemType.BOOSTER ? "⚡"
                : item.getName().isEmpty() ? "?" : item.getName().substring(0, 1).toUpperCase();
        Label lbl = new Label(iconText);
        lbl.setStyle("-fx-font-size: 36px;");
        return lbl;
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

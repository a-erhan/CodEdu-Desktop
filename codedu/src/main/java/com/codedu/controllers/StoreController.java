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

@Controller
@Scope("prototype")
public class StoreController {

    @FXML private VBox storeContent;

    private User user;
    private List<Item> allItems = List.of();
    private Consumer<User> onUserUpdated;

    @Autowired private StoreService storeService;
    @Autowired private ItemService itemService;
    @Autowired private InventoryItemService inventoryItemService;

    private final String LOGO_BLUE = "#00AEEF";
    private final String LOGO_ORANGE = "#F7941D";
    private final String CARD_BG = "#3b4252";
    private final String CARD_BORDER = "#4c566a";

    public void setOnUserUpdated(Consumer<User> onUserUpdated) {
        this.onUserUpdated = onUserUpdated;
    }

    public void setUserModel(User user) {
        if (user == null) return;
        this.user = user;
        allItems = itemService.getAllItemEntities();
        markOwnedItemsFromInventory();
        buildGrid();
    }

    private void markOwnedItemsFromInventory() {
        if (user == null || user.getId() <= 0) return;
        for (Item storeItem : allItems) {
            if (storeItem == null || storeItem.getId() <= 0) continue;
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
            Label categoryLabel = new Label(categoryTitle(entry.getKey()).toUpperCase());
            categoryLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold; -fx-letter-spacing: 0.1em; -fx-font-size: 14px;");
            categoryLabel.setPadding(new Insets(20, 0, 10, 5));
            storeContent.getChildren().add(categoryLabel);

            FlowPane grid = new FlowPane(20, 20);
            grid.setAlignment(Pos.TOP_LEFT);
            for (Item item : entry.getValue()) {
                grid.getChildren().add(buildItemCard(item));
            }
            storeContent.getChildren().add(grid);
        }
    }

    private VBox buildItemCard(Item item) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 15; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 15; -fx-border-width: 1;");
        card.setPadding(new Insets(25, 20, 20, 20));
        card.setPrefWidth(210); card.setMinWidth(210); card.setMaxWidth(210);
        card.setAlignment(Pos.TOP_CENTER);

        javafx.scene.Node iconNode = buildItemIcon(item);

        Label name = new Label(item.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);

        Label desc = new Label(item.getDescription());
        desc.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px;");
        desc.setWrapText(true);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        desc.setMinHeight(36);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox priceRow = new HBox(6);
        priceRow.setAlignment(Pos.CENTER);
        Label coinIcon = new Label("🪙");
        Label priceLabel = new Label(String.valueOf(item.getPrice()));
        priceLabel.setStyle("-fx-text-fill: " + LOGO_BLUE + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        priceRow.getChildren().addAll(coinIcon, priceLabel);

        boolean isOneTimeType = (item.getType() == ItemType.AVATAR);
        boolean showAsOwned = isOneTimeType && item.isOwned();

        Button buyBtn = new Button();
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setCursor(javafx.scene.Cursor.HAND);

        if (showAsOwned) {
            buyBtn.setText("OWNED");
            buyBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #a3be8c; -fx-border-radius: 20; -fx-text-fill: #a3be8c; -fx-font-weight: bold;");
            buyBtn.setDisable(true);
        } else {
            buyBtn.setText("BUY");
            buyBtn.setStyle("-fx-background-color: " + LOGO_BLUE + "; -fx-text-fill: #2e3440; -fx-background-radius: 20; -fx-font-weight: bold;");
        }

        if (!showAsOwned) {
            buyBtn.setOnAction(e -> {
                if (user != null && user.getGameState() != null && user.getGameState().hasEnoughTokens(item.getPrice())) {
                    User updated = storeService.purchaseItem(user.getId(), item.getId()).orElse(null);
                    if (updated != null) {
                        user = updated;
                    } else {
                        String nm = item.getName() != null ? item.getName().toLowerCase() : "";
                        UserGameState gs = user.getGameState();
                        boolean heartsFull = nm.contains("heart") && gs != null && gs.getHeartCount() >= UserGameState.MAX_HEARTS;
                        Alert alert = new Alert(heartsFull ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
                        alert.setTitle("Store Notice");
                        alert.setHeaderText(null);
                        if (heartsFull) {
                            alert.setContentText("Your hearts are already full!");
                        } else {
                            alert.setContentText("Insufficient tokens.");
                        }
                        alert.showAndWait();
                    }

                    if (isOneTimeType) {
                        item.setOwned(true);
                        buyBtn.setText("OWNED");
                        buyBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #a3be8c; -fx-border-radius: 20; -fx-text-fill: #a3be8c; -fx-font-weight: bold;");
                        buyBtn.setOnAction(null);
                        buyBtn.setDisable(true);
                    }

                    if (updated != null && onUserUpdated != null) {
                        onUserUpdated.accept(user);
                    }

                    ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                    st.setToX(1.05); st.setToY(1.05); st.setAutoReverse(true); st.setCycleCount(2);
                    st.play();
                }
            });
        }

        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.03); st.setToY(1.03);
            card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 15; -fx-border-color: " + LOGO_BLUE + "; -fx-border-radius: 15; -fx-border-width: 1;");
            st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.0); st.setToY(1.0);
            card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 15; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 15; -fx-border-width: 1;");
            st.play();
        });

        card.getChildren().addAll(iconNode, name, desc, spacer, priceRow, buyBtn);
        return card;
    }

    private javafx.scene.Node buildItemIcon(Item item) {
        if (item.getType() == ItemType.AVATAR) {
            String name = item.getName().toLowerCase();
            String file = name.contains("ninja") ? "avatar_ninja.png" : name.contains("wizard") ? "avatar_wizard.png" : "avatar_basic.png";
            try {
                var url = getClass().getResource("/com/codedu/images/avatars/" + file);
                if (url != null) {
                    Image img = new Image(url.toExternalForm());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(80); iv.setFitHeight(80); iv.setPreserveRatio(true);
                    iv.setClip(new Circle(40, 40, 40));
                    return iv;
                }
            } catch (Exception ignored) {}
        }
        String iconText = item.getType() == ItemType.AI_USAGE ? "🤖" : item.getType() == ItemType.BOOSTER ? "⚡" : item.getName().isEmpty() ? "?" : item.getName().substring(0, 1).toUpperCase();
        Label lbl = new Label(iconText);
        lbl.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");
        return lbl;
    }

    private String categoryTitle(ItemType c) {
        return switch (c) {
            case AVATAR -> "Avatars";
            case BOOSTER -> "Power-ups";
            case AI_USAGE -> "AI Features";
            default -> c.name();
        };
    }
}
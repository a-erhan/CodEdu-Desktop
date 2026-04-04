package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.InventoryItemService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Lists {@link InventoryItem} rows for the logged-in user and supports equip toggles for eligible items.
 */
@Controller
public class InventoryItemController {

    @FXML
    private VBox inventoryList;
    @FXML
    private Label inventorySummaryLabel;

    private User user;

    @Autowired
    private InventoryItemService inventoryItemService;

    public void setUserModel(User user) {
        this.user = user;
        refresh();
    }

    private void refresh() {
        inventoryList.getChildren().clear();
        if (user == null) {
            inventorySummaryLabel.setText("—");
            return;
        }

        List<InventoryItem> rows = inventoryItemService.getItemEntitiesForUser(user);
        inventorySummaryLabel.setText(rows.size() + " items");

        if (rows.isEmpty()) {
            Label empty = new Label("No items in your inventory yet. Visit the Store or Item catalog.");
            empty.getStyleClass().add(Styles.TEXT_MUTED);
            empty.setWrapText(true);
            inventoryList.getChildren().add(empty);
            return;
        }

        for (InventoryItem row : rows) {
            inventoryList.getChildren().add(buildRow(row));
        }
    }

    private HBox buildRow(InventoryItem invItem) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

        String name = invItem.getItem() != null && invItem.getItem().getName() != null
                ? invItem.getItem().getName()
                : "Item #" + invItem.getId();

        Label title = new Label(name);
        title.getStyleClass().add(Styles.TEXT_BOLD);

        Label qty = new Label("× " + invItem.getQuantity());
        qty.getStyleClass().add(Styles.TEXT_SUBTLE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label equippedBadge = new Label(invItem.isEquipped() ? "Equipped" : "");
        if (invItem.isEquipped()) {
            equippedBadge.getStyleClass().addAll(Styles.TEXT_SUBTLE, Styles.TEXT_BOLD);
        }

        boolean equipable = invItem.getItem() != null && invItem.getItem().getType() == ItemType.AVATAR;
        Button toggleBtn = new Button(invItem.isEquipped() ? "Unequip" : "Equip");
        toggleBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED, Styles.SMALL);
        toggleBtn.setVisible(equipable);
        toggleBtn.setManaged(equipable);
        equippedBadge.setVisible(equipable && invItem.isEquipped());
        equippedBadge.setManaged(equipable);
        toggleBtn.setOnAction(e -> {
            if (user == null) {
                return;
            }
            inventoryItemService.setEquipped(invItem, !invItem.isEquipped());
            refresh();
        });

        row.getChildren().addAll(title, qty, spacer, equippedBadge, toggleBtn);
        return row;
    }
}

package com.codedu.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a user's inventory (from UML diagram).
 */
public class UserInventory {

    private Long id;
    private Long userId;
    private List<InventoryItem> items;

    public UserInventory() {
        this.items = new ArrayList<>();
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- User ID ---
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // --- Items ---
    public List<InventoryItem> getItems() {
        return items;
    }

    public void setItems(List<InventoryItem> items) {
        this.items = items;
    }

    public void addItem(InventoryItem item) {
        this.items.add(item);
    }
    public void removeItem(InventoryItem item) {
        this.items.remove(item);
    }

}

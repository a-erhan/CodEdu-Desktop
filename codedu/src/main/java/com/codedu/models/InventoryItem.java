package com.codedu.models;

import java.time.LocalDateTime;

/**
 * Model representing an item in a user's inventory (from UML diagram).
 */
public class InventoryItem {

    private Long id;
    private Item item;
    private int quantity;
    private boolean isEquipped;
    private LocalDateTime acquiredAt;

    public InventoryItem() {
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Item ---
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    // --- Quantity ---
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // --- Is Equipped ---
    public boolean isEquipped() {
        return isEquipped;
    }

    public void setEquipped(boolean equipped) {
        isEquipped = equipped;
    }

    // --- Acquired At ---
    public LocalDateTime getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(LocalDateTime acquiredAt) {
        this.acquiredAt = acquiredAt;
    }
}

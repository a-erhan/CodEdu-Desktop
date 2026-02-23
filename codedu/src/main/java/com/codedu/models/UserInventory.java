package com.codedu.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a user's inventory (from UML diagram).
 */
@Entity
@Table(name = "user_inventories")
public class UserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "inventory")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
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

    // --- User ---
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

package com.codedu.models;

import jakarta.persistence.*;

/**
 * Model representing an item in the store (from UML diagram).
 */
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String emoji;
    private int price;

    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Transient
    private boolean owned;

    public Item() {
    }

    public Item(String name, String description, String emoji, int price, ItemType type) {
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.price = price;
        this.type = type;
        this.owned = false;
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Name ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- Description ---
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- Emoji ---
    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    // --- Price ---
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    // --- Type ---
    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    // --- Owned (UI-only, not persisted) ---
    public boolean isOwned() {
        return owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }
}

package com.codedu.models;

/**
 * Represents a purchasable item in the CodEdu Store.
 */
public class StoreItem {

    public enum Category {
        AVATAR, THEME, POWER_UP, BUNDLE
    }

    private final String name;
    private final String description;
    private final String emoji;
    private final int price;
    private final Category category;
    private boolean owned;

    public StoreItem(String name, String description, String emoji, int price, Category category) {
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.price = price;
        this.category = category;
        this.owned = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isOwned() {
        return owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }
}

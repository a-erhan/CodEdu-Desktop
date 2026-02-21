package com.codedu.models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model representing user profile data including tokens, XP, badge info,
 * owned store items, equipped avatar, and theme preference.
 */
public class UserModel {

    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty badgeIcon = new SimpleStringProperty();
    private final IntegerProperty tokenBalance = new SimpleIntegerProperty();
    private final IntegerProperty currentXP = new SimpleIntegerProperty();
    private final IntegerProperty maxXP = new SimpleIntegerProperty();
    private final StringProperty equippedAvatar = new SimpleStringProperty();
    private final BooleanProperty darkMode = new SimpleBooleanProperty(true);
    private final ObservableList<StoreItem> ownedItems = FXCollections.observableArrayList();

    public UserModel() {
        this.username.set("CodeWarrior42");
        this.badgeIcon.set("\uD83C\uDFC5");
        this.tokenBalance.set(1500);
        this.currentXP.set(720);
        this.maxXP.set(1000);
        this.equippedAvatar.set("\uD83E\uDDD1\u200D\uD83D\uDCBB"); // 🧑‍💻
    }

    // --- Username ---
    public String getUsername() {
        return username.get();
    }

    public void setUsername(String value) {
        username.set(value);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    // --- Badge Icon ---
    public String getBadgeIcon() {
        return badgeIcon.get();
    }

    public void setBadgeIcon(String value) {
        badgeIcon.set(value);
    }

    public StringProperty badgeIconProperty() {
        return badgeIcon;
    }

    // --- Token Balance ---
    public int getTokenBalance() {
        return tokenBalance.get();
    }

    public void setTokenBalance(int value) {
        tokenBalance.set(value);
    }

    public IntegerProperty tokenBalanceProperty() {
        return tokenBalance;
    }

    // --- Current XP ---
    public int getCurrentXP() {
        return currentXP.get();
    }

    public void setCurrentXP(int value) {
        currentXP.set(value);
    }

    public IntegerProperty currentXPProperty() {
        return currentXP;
    }

    // --- Max XP ---
    public int getMaxXP() {
        return maxXP.get();
    }

    public void setMaxXP(int value) {
        maxXP.set(value);
    }

    public IntegerProperty maxXPProperty() {
        return maxXP;
    }

    // --- Equipped Avatar ---
    public String getEquippedAvatar() {
        return equippedAvatar.get();
    }

    public void setEquippedAvatar(String value) {
        equippedAvatar.set(value);
    }

    public StringProperty equippedAvatarProperty() {
        return equippedAvatar;
    }

    // --- Dark Mode ---
    public boolean isDarkMode() {
        return darkMode.get();
    }

    public void setDarkMode(boolean value) {
        darkMode.set(value);
    }

    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    // --- Owned Items ---
    public ObservableList<StoreItem> getOwnedItems() {
        return ownedItems;
    }

    /**
     * Returns XP progress as a value between 0.0 and 1.0.
     */
    public double getXPProgress() {
        if (maxXP.get() == 0)
            return 0;
        return (double) currentXP.get() / maxXP.get();
    }

    /**
     * Purchase a store item: deduct tokens, mark owned, add to collection.
     * 
     * @return true if purchase succeeded, false if insufficient tokens or already
     *         owned.
     */
    public boolean buyItem(StoreItem item) {
        if (item.isOwned() || tokenBalance.get() < item.getPrice()) {
            return false;
        }
        tokenBalance.set(tokenBalance.get() - item.getPrice());
        item.setOwned(true);
        ownedItems.add(item);
        return true;
    }

    /**
     * Equip an avatar item (must be owned).
     */
    public void equipAvatar(StoreItem item) {
        if (item.isOwned() && item.getCategory() == StoreItem.Category.AVATAR) {
            equippedAvatar.set(item.getEmoji());
        }
    }
}
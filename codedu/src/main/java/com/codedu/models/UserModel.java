<<<<<<< HEAD
package com.codedu.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model representing user profile data including tokens, XP, and badge info.
 * Uses JavaFX observable properties for easy binding to UI elements.
 */
public class UserModel {

    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty badgeIcon = new SimpleStringProperty();
    private final IntegerProperty tokenBalance = new SimpleIntegerProperty();
    private final IntegerProperty currentXP = new SimpleIntegerProperty();
    private final IntegerProperty maxXP = new SimpleIntegerProperty();

    public UserModel() {
        // Default sample data for the shell demo
        this.username.set("CodeWarrior42");
        this.badgeIcon.set("\uD83C\uDFC5"); // 🏅 medal emoji
        this.tokenBalance.set(1500);
        this.currentXP.set(720);
        this.maxXP.set(1000);
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

    /**
     * Returns XP progress as a value between 0.0 and 1.0.
     */
    public double getXPProgress() {
        if (maxXP.get() == 0) return 0;
        return (double) currentXP.get() / maxXP.get();
    }
}
=======
package com.codedu.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model representing user profile data including tokens, XP, and badge info.
 * Uses JavaFX observable properties for easy binding to UI elements.
 */
public class UserModel {

    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty badgeIcon = new SimpleStringProperty();
    private final IntegerProperty tokenBalance = new SimpleIntegerProperty();
    private final IntegerProperty currentXP = new SimpleIntegerProperty();
    private final IntegerProperty maxXP = new SimpleIntegerProperty();

    public UserModel() {
        // Default sample data for the shell demo
        this.username.set("CodeWarrior42");
        this.badgeIcon.set("\uD83C\uDFC5"); // 🏅 medal emoji
        this.tokenBalance.set(1500);
        this.currentXP.set(720);
        this.maxXP.set(1000);
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

    /**
     * Returns XP progress as a value between 0.0 and 1.0.
     */
    public double getXPProgress() {
        if (maxXP.get() == 0) return 0;
        return (double) currentXP.get() / maxXP.get();
    }
}
>>>>>>> 68084a056cecabbca83ad1f2bea89e41b0d5769a

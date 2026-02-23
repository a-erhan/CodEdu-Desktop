package com.codedu.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an achievement/badge in the system.
 */
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String badgeName;
    private String description;
    private int tokenReward;
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "achievements", fetch = FetchType.LAZY)
    private List<UserGameState> usersAchieved;

    private String iconPath;

    public Achievement() {
        this.usersAchieved = new ArrayList<>();
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Badge Name ---
    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    // --- Description ---
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- Token Reward ---
    public int getTokenReward() {
        return tokenReward;
    }

    public void setTokenReward(int tokenReward) {
        this.tokenReward = tokenReward;
    }

    // --- Created At ---
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Users Achieved ---
    public List<UserGameState> getUsersAchieved() {
        return usersAchieved;
    }

    public void setUsersAchieved(List<UserGameState> usersAchieved) {
        this.usersAchieved = usersAchieved;
    }

    public void addUsersAchieved(UserGameState userGameState) {
        this.usersAchieved.add(userGameState);
    }

    // --- Icon Path ---
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}

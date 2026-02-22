package com.codedu.models;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

public class Achievement {
    private Long id;
    private String badgeName;
    private String description;
    private int tokenReward;
    private LocalDateTime createdAt;
    private List<UserGameState> usersAchieved;
    private ImageIcon icon;


    //Getters-Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getBadgeName() {
        return badgeName;
    }
    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getTokenReward() {
        return tokenReward;
    }
    public void setTokenReward(int tokenReward) {
        this.tokenReward = tokenReward;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public List<UserGameState> getUsersAchieved() {
        return usersAchieved;
    }
    public void setUsersAchieved(List<UserGameState> usersAchieved) {
        this.usersAchieved = usersAchieved;
    }
    public ImageIcon getIcon() {
        return icon;
    }
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    //Special methods
    public void addUsersAchieved(UserGameState userGameState) {
        this.usersAchieved.add(userGameState);
    }
}


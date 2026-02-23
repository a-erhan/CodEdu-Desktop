package com.codedu.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a user's game progression state.
 */
@Entity
@Table(name = "user_game_states")
public class UserGameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int heartCount;
    private int level;
    private int xp;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_achievements", joinColumns = @JoinColumn(name = "user_game_state_id"), inverseJoinColumns = @JoinColumn(name = "achievement_id"))
    private List<Achievement> achievements;

    public UserGameState() {
        this.achievements = new ArrayList<>();
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

    // --- Heart Count ---
    public int getHeartCount() {
        return heartCount;
    }

    public void setHeartCount(int heartCount) {
        this.heartCount = heartCount;
    }

    // --- Level ---
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // --- XP ---
    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    // --- Achievements ---
    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public void addAchievement(Achievement achievement) {
        this.achievements.add(achievement);
    }
}

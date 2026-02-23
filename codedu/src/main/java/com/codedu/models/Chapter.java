package com.codedu.models;

import jakarta.persistence.*;

/**
 * Model representing a single chapter/lesson in a learning path.
 */
@Entity
@Table(name = "chapters")
public class Chapter {

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String iconEmoji;
    private String iconImage; // resource path e.g. "/com/codedu/images/ch_hello_world.png"

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private int totalLessons;
    private int completedLessons;
    private int xpReward;
    private boolean locked;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private ChapterContent content;

    public Chapter() {
    }

    public Chapter(String title, String description, String iconEmoji,
            String iconImage, Difficulty difficulty, int totalLessons,
            int completedLessons, int xpReward, boolean locked) {
        this.title = title;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.iconImage = iconImage;
        this.difficulty = difficulty;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
        this.xpReward = xpReward;
        this.locked = locked;
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Title ---
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // --- Description ---
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- Icon Emoji ---
    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    // --- Icon Image ---
    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    // --- Difficulty ---
    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    // --- Total Lessons ---
    public int getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
    }

    // --- Completed Lessons ---
    public int getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(int completedLessons) {
        this.completedLessons = completedLessons;
    }

    // --- XP Reward ---
    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    // --- Locked ---
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    // --- Computed ---
    public boolean isCompleted() {
        return !locked && completedLessons >= totalLessons;
    }

    public double getProgress() {
        if (totalLessons == 0)
            return 0;
        return (double) completedLessons / totalLessons;
    }

    // --- Content ---
    public ChapterContent getContent() {
        return content;
    }

    public void setContent(ChapterContent content) {
        this.content = content;
    }
}
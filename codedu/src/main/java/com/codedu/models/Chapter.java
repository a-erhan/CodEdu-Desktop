package com.codedu.models;

/**
 * Model representing a single chapter/lesson in a learning path.
 */
public class Chapter {

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    private final String id;
    private final String title;
    private final String description;
    private final String iconEmoji;
    private final String iconImage; // resource path e.g. "/com/codedu/images/ch_hello_world.png"
    private final Difficulty difficulty;
    private final int totalLessons;
    private final int completedLessons;
    private final int xpReward;
    private final boolean locked;
    private ChapterContent content;

    public Chapter(String id, String title, String description, String iconEmoji,
            String iconImage, Difficulty difficulty, int totalLessons,
            int completedLessons, int xpReward, boolean locked) {
        this.id = id;
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

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public String getIconImage() {
        return iconImage;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public int getXpReward() {
        return xpReward;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isCompleted() {
        return !locked && completedLessons >= totalLessons;
    }

    public double getProgress() {
        if (totalLessons == 0)
            return 0;
        return (double) completedLessons / totalLessons;
    }

    public ChapterContent getContent() {
        return content;
    }

    public void setContent(ChapterContent content) {
        this.content = content;
    }
}
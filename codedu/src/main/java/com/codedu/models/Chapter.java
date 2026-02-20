package com.codedu.models;

/**
 * Model representing a single chapter/lesson in a learning path.
 */
public class Chapter {

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    private final String title;
    private final String description;
    private final String iconEmoji;
    private final Difficulty difficulty;
    private final int totalLessons;
    private final int completedLessons;
    private final int xpReward;
    private final boolean locked;

    public Chapter(String title, String description, String iconEmoji,
            Difficulty difficulty, int totalLessons, int completedLessons,
            int xpReward, boolean locked) {
        this.title = title;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.difficulty = difficulty;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
        this.xpReward = xpReward;
        this.locked = locked;
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
}

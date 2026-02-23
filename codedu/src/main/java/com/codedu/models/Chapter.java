package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Model representing a single chapter/lesson in a learning path.
 */
@Entity
@Table(name = "chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter extends BaseEntity {

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    private String title;
    private String description;
    private String iconEmoji;
    private String iconImage;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private int totalLessons;
    private int completedLessons;
    private int xpReward;
    private boolean locked;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private ChapterContent content;

    public boolean isCompleted() {
        return !locked && completedLessons >= totalLessons;
    }

    public double getProgress() {
        if (totalLessons == 0) return 0;
        return (double) completedLessons / totalLessons;
    }
}
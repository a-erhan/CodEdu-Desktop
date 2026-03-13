package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id")
    private LearningPath path;

    private String topicName;

    public Chapter(String title,
                   String description,
                   String iconEmoji,
                   String iconImage,
                   Difficulty difficulty,
                   int totalLessons,
                   int completedLessons,
                   int xpReward,
                   boolean locked) {

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    private int tokenReward;

    public boolean isCompleted(User user) {
        return questions != null && !questions.isEmpty();
    }
}
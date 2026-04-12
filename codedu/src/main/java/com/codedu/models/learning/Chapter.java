package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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
    private int xpReward;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "content_id")
    private ChapterContent content;

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
                   int xpReward) {

        this.title = title;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.iconImage = iconImage;
        this.difficulty = difficulty;
        this.totalLessons = totalLessons;
        this.xpReward = xpReward;
    }

    private int tokenReward;

    @Column(name = "order_index")
    private int orderIndex;

}
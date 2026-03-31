package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
import com.codedu.models.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_chapter_progress",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "chapter_id"}) // Prevents duplicate progress records
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChapterProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "completed_lessons", nullable = false)
    @Builder.Default
    private int completedLessons = 0;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean isCompleted = false;

    @Column(name = "is_unlocked", nullable = false)
    @Builder.Default
    private boolean isUnlocked = false;

    public void incrementProgress() {
        // Use getChapter() instead of this.chapter
        if (!this.isCompleted && getChapter() != null) {
            this.completedLessons++;

            if (this.completedLessons >= getChapter().getTotalLessons()) {
                this.isCompleted = true;
                this.completedLessons = getChapter().getTotalLessons();
            }
        }
    }

    public double getProgressPercentage() {
        int total = this.chapter.getTotalLessons();
        if (total == 0) return 0.0;
        return (double) this.completedLessons / total;
    }
}
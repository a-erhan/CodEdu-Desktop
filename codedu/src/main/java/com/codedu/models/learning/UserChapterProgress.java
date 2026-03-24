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


    public void incrementProgress() {
        if (!this.isCompleted) {
            this.completedLessons++;

            // If the user has finished all lessons in the chapter, mark it as complete
            if (this.completedLessons >= this.chapter.getTotalLessons()) {
                this.isCompleted = true;
                this.completedLessons = this.chapter.getTotalLessons(); // Cap it just in case
            }
        }
    }

    public double getProgressPercentage() {
        int total = this.chapter.getTotalLessons();
        if (total == 0) return 0.0;
        return (double) this.completedLessons / total;
    }
}
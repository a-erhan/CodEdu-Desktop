package com.codedu.dtos;

import com.codedu.models.learning.Chapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterProgressDTO {
    private Chapter chapter;
    private int completedLessons;
    private boolean isLocked;
    private boolean isCompleted;


    public double getProgress() {
        if (chapter == null || chapter.getTotalLessons() == 0) return 0.0;
        return (double) completedLessons / chapter.getTotalLessons();
    }


}
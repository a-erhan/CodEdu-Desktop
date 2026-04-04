package com.codedu.dtos;

import com.codedu.dtos.learning.ChapterDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterProgressDTO {
    // 🚀 Changed from Chapter (Entity) to ChapterDTO
    private ChapterDTO chapter;
    private int completedLessons;
    private boolean isLocked;
    private boolean isCompleted;
    private int dynamicTotalLessons;

    public double getProgress() {
        if (chapter == null) return 0.0;
        int total = (dynamicTotalLessons > 0) ? dynamicTotalLessons : chapter.totalLessons();
        if (total <= 0) return 0.0;
        return (double) completedLessons / total;
    }
}
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

    // 🚀 1. ADD THIS FIELD: A safe place to store the real count
    // without ever touching the database!
    private int dynamicTotalLessons;

    public double getProgress() {
        if (chapter == null) return 0.0;

        // 🚀 2. THE MATH FIX: Use dynamic count if we found it, otherwise fallback to DB
        int total = (dynamicTotalLessons > 0) ? dynamicTotalLessons : chapter.getTotalLessons();

        if (total <= 0) return 0.0; // Prevent divide by zero!

        return (double) completedLessons / total;
    }
}
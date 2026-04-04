package com.codedu.dtos.learning;

import com.codedu.models.learning.Chapter;
import lombok.Builder;
import java.util.List;

@Builder
public record ChapterDTO(
        int id,
        String title,
        String description,
        String iconEmoji,
        String iconImage,
        String learnText, // 🚀 Added for the Learn Tab
        Chapter.Difficulty difficulty,
        int totalLessons,
        int xpReward,
        int tokenReward,
        int orderIndex,
        String topicName,
        List<QuestionDTO> questions // 🚀 Added so ChapterView can build the UI
) {}
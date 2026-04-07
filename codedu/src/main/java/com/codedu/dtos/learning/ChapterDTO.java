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
        String learnText,
        Chapter.Difficulty difficulty,
        int totalLessons,
        int xpReward,
        int tokenReward,
        int orderIndex,
        String topicName,
        List<QuestionDTO> questions
) {}
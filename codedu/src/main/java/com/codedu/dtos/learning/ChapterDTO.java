package com.codedu.dtos.learning;

import com.codedu.models.learning.Chapter;
import lombok.Builder;

@Builder
public record ChapterDTO(
    int id,
    String title,
    String description,
    String iconEmoji,
    String iconImage,
    Chapter.Difficulty difficulty,
    int totalLessons,
    int xpReward,
    int tokenReward,
    int orderIndex,
    String topicName
) {}

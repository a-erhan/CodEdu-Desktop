package com.codedu.dtos.learning;

import com.codedu.models.learning.Chapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDTO {
    private int id;
    private String title;
    private String description;
    private String iconEmoji;
    private String iconImage;
    private Chapter.Difficulty difficulty;
    private int totalLessons;
    private int xpReward;
    private int tokenReward;
    private int orderIndex;
    private String topicName;
}

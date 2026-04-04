package com.codedu.dtos.learning;

import lombok.Builder;
import java.util.List;

@Builder
public record LearningPathDTO(
    int id,
    String title,
    String description,
    List<ChapterDTO> chapters
) {}

package com.codedu.dtos.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathDTO {
    private int id;
    private String title;
    private String description;
    private List<ChapterDTO> chapters;
}

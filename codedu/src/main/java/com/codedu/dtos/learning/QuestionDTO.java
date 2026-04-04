package com.codedu.dtos.learning;

import com.codedu.models.learning.QuestionDifficulty;
import com.codedu.models.learning.QuestionType;
import lombok.Builder;
import java.util.List;

@Builder
public record QuestionDTO(
    int id,
    String title,
    String content,
    String hint,
    QuestionType questionType,
    QuestionDifficulty questionDifficulty,
    int rewardXp,
    int rewardToken,
    String boilerplateCode,
    List<TestCaseDTO> testCases,
    List<String> choices
) {
    @Builder
    public record TestCaseDTO(
        int id,
        String input,
        String expectedOutput,
        boolean isHidden,
        float cpuTimeLimit
    ) {}
}

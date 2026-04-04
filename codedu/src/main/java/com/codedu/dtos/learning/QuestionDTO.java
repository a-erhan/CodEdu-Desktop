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
        String solution, // 🚀 Added so the Give Up logic works
        QuestionType questionType,
        QuestionDifficulty questionDifficulty,
        int rewardXp,    // 🚀 Flattened from Reward entity
        int rewardToken, // 🚀 Flattened from Reward entity
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
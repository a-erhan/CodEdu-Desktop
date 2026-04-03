package com.codedu.dtos.learning;

import com.codedu.models.learning.QuestionDifficulty;
import com.codedu.models.learning.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private int id;
    private String title;
    private String content;
    private String hint;
    private QuestionType questionType;
    private QuestionDifficulty questionDifficulty;
    private int rewardXp;
    private int rewardToken;

    // CodeImplementationQuestion specific
    private String boilerplateCode;
    private List<TestCaseDTO> testCases;

    // MultipleChoiceQuestion specific
    private List<String> choices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseDTO {
        private int id;
        private String input;
        private String expectedOutput;
        private boolean isHidden;
        private float cpuTimeLimit;
    }
}

package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {

    private int difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Column(columnDefinition = "TEXT")
    private String hint;

    private int tokenReward;
    private int xpReward;

    public Question(QuestionType type, String content, String title, String solution, String hint, int tokenReward, int xpReward) {
        this.questionType = type;
        this.content = content;
        this.title = title;
        this.solution = solution;
        this.hint = hint;
        this.tokenReward = tokenReward;
        this.xpReward = xpReward;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = (difficulty > 0 && difficulty <= 100) ? difficulty : 1;
    }

    public boolean validateAnswer(String userAnswer) {
        if (this.solution == null) return false;
        return this.solution.trim().equalsIgnoreCase(userAnswer.trim());
    }
}
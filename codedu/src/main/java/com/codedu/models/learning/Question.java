package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)

public abstract class Question extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "question_difficulty")
    private QuestionDifficulty questionDifficulty;

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

    @Embedded
    private Reward reward;

    public Question(QuestionType questionType,
                    String content,
                    String title,
                    String solution,
                    String hint,
                    Reward reward) {

        this.questionType = questionType;
        this.content = content;
        this.title = title;
        this.solution = solution;
        this.hint = hint;
        this.reward = reward;
    }

    public boolean validateAnswer(String userAnswer) {
        if (this.solution == null) return false;
        return this.solution.trim().equalsIgnoreCase(userAnswer.trim());
    }
}
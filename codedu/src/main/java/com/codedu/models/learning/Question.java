package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "questionType", defaultImpl = CodeImplementationQuestion.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CodeImplementationQuestion.class, name = "CODE_IMPLEMENTATION"),
        @JsonSubTypes.Type(value = MultipleChoiceQuestion.class, name = "MULTIPLE_CHOICES"),
        @JsonSubTypes.Type(value = FillInBlankQuestion.class, name = "FILL_IN_THE_BLANKS")
})
public abstract class Question extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_content_id")
    private ChapterContent chapterContent;

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
        if (this.solution == null)
            return false;
        return this.solution.trim().equalsIgnoreCase(userAnswer.trim());
    }
}
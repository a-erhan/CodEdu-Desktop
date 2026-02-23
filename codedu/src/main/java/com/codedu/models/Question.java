package com.codedu.models;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "token_reward")
    private int tokenReward;

    @Column(name = "xp_reward")
    private int xpReward;

    public Question() {}

    public Question(QuestionType type, String content, String title, String solution, String hint, int tokenReward, int xpReward) {
        this.questionType = type;
        this.content = content;
        this.title = title;
        this.solution = solution;
        this.hint = hint;
        this.tokenReward = tokenReward;
        this.xpReward = xpReward;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        if (difficulty > 0 && difficulty <= 100) {
            this.difficulty = difficulty;
        } else {
            this.difficulty = 1;
        }
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getTokenReward() {
        return tokenReward;
    }

    public void setTokenReward(int tokenReward) {
        this.tokenReward = tokenReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public boolean validateAnswer() {
        return false;
    }
}
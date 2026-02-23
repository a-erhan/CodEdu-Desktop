package com.codedu.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a task assigned to a match.
 */
@Entity
@Table(name = "match_tasks")
public class MatchTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "match_task_questions", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "question_id")
    private List<Long> questionIds;

    private int xpReward;
    private int tokenReward;

    public MatchTask() {
        this.questionIds = new ArrayList<>();
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Token Reward ---
    public int getTokenReward() {
        return tokenReward;
    }

    public void setTokenReward(int tokenReward) {
        this.tokenReward = tokenReward;
    }

    // --- XP Reward ---
    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    // --- Question IDs ---
    public List<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds;
    }

    public void addQuestionId(Long questionId) {
        questionIds.add(questionId);
    }

    public void removeQuestionId(Long questionId) {
        questionIds.remove(questionId);
    }
}

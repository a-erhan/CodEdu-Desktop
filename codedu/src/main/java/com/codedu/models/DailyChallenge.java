package com.codedu.models;

import jakarta.persistence.*;

@Entity
public class DailyChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "xp_rewards")
    private int xpRewards;

    @Column(name = "token_rewards")
    private int tokenRewards;

    public DailyChallenge() {
    }

    public DailyChallenge(Question question, String name, String description, int xpRewards, int tokenRewards) {
        this.question = question;
        this.name = name;
        this.description = description;
        this.xpRewards = xpRewards;
        this.tokenRewards = tokenRewards;
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // -- Name --
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --Description--
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --Question--
    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    // --Rewards--
    public int getXpRewards() {
        return xpRewards;
    }

    public void setXpRewards(int xpRewards) {
        this.xpRewards = xpRewards;
    }

    public int getTokenRewards() {
        return tokenRewards;
    }

    public void setTokenRewards(int tokenRewards) {
        this.tokenRewards = tokenRewards;
    }

}

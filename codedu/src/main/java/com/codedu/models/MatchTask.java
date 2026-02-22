package com.codedu.models;

import java.util.Collections;
import java.util.List;

public class MatchTask {
    private long id;
    private List<Long> questionIds;
    private int xpReward, tokenReward;

    public int getTokenReward() {
        return tokenReward;
    }

    public void setTokenReward(int tokenReward) {
        this.tokenReward = tokenReward;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

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

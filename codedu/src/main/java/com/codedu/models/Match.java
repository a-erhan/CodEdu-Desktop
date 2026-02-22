package com.codedu.models;

import java.time.LocalDateTime;

public class Match {
    private Long id;
    private Competitor competitor1, competitor2;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double duration;
    private double player1Time;
    private double player2Time;
    private int reward;
    private MatchStatus status;
    private MatchTask task;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Competitor[] getCompetitors() {
        return new Competitor[]{competitor1, competitor2};
    }
    public void setCompetitors(Competitor cp1, Competitor cp2) {
        competitor1 = cp1;
        competitor2 = cp2;
    }
}


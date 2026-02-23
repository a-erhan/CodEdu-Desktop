package com.codedu.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Model representing a competitive match between two competitors.
 */
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor1_id")
    private Competitor competitor1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor2_id")
    private Competitor competitor2;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double duration;
    private double player1Time;
    private double player2Time;
    private int reward;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private MatchTask task;

    public Match() {
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Competitor 1 ---
    public Competitor getCompetitor1() {
        return competitor1;
    }

    public void setCompetitor1(Competitor competitor1) {
        this.competitor1 = competitor1;
    }

    // --- Competitor 2 ---
    public Competitor getCompetitor2() {
        return competitor2;
    }

    public void setCompetitor2(Competitor competitor2) {
        this.competitor2 = competitor2;
    }

    // --- Convenience accessors (backward compatible) ---
    public Competitor[] getCompetitors() {
        return new Competitor[] { competitor1, competitor2 };
    }

    public void setCompetitors(Competitor cp1, Competitor cp2) {
        this.competitor1 = cp1;
        this.competitor2 = cp2;
    }

    // --- Start Time ---
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    // --- End Time ---
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    // --- Duration ---
    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    // --- Player 1 Time ---
    public double getPlayer1Time() {
        return player1Time;
    }

    public void setPlayer1Time(double player1Time) {
        this.player1Time = player1Time;
    }

    // --- Player 2 Time ---
    public double getPlayer2Time() {
        return player2Time;
    }

    public void setPlayer2Time(double player2Time) {
        this.player2Time = player2Time;
    }

    // --- Reward ---
    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    // --- Status ---
    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    // --- Task ---
    public MatchTask getTask() {
        return task;
    }

    public void setTask(MatchTask task) {
        this.task = task;
    }
}

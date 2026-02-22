package com.codedu.models;

/**
 * Model representing a competitor in the system (from UML diagram).
 */
public class Competitor {

    private Long id;
    private Long userId;
    private int rankingPoint;
    private int totalWins;
    private int totalLosses;
    private int totalMatches;

    public Competitor() {
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- User ID ---
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // --- Ranking Point ---
    public int getRankingPoint() {
        return rankingPoint;
    }

    public void setRankingPoint(int rankingPoint) {
        this.rankingPoint = rankingPoint;
    }

    // --- Total Wins ---
    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    // --- Total Losses ---
    public int getTotalLosses() {
        return totalLosses;
    }

    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }

    // --- Total Matches ---
    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }
}

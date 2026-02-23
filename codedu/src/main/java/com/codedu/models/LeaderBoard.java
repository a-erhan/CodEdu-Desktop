package com.codedu.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leaderboards")
public class LeaderBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdate;

    private int userRank;

    @ManyToMany
    @JoinTable(name = "leaderboard_competitors", joinColumns = @JoinColumn(name = "leaderboard_id"), inverseJoinColumns = @JoinColumn(name = "competitor_id"))
    private List<Competitor> competitors;
    private int requiredLevel;

    public LeaderBoard() {
        this.competitors = new ArrayList<Competitor>();
    }

    // --ID--
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --Name--
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --Time--
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    // -- User Rank --
    public int getUserRank() {
        return userRank;
    }

    public void setUserRank(int userRank) {
        this.userRank = userRank > 0 ? userRank : 0;
    }

    // --Competitor--
    public List<Competitor> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(List<Competitor> competitors) {
        this.competitors = competitors;
    }

    public void addCompetitor(Competitor competitor) {
        this.competitors.add(competitor);
    }

    // --Level--
    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

}

package com.codedu.models;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_making_queue")
public class MatchMakingQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "queue_competitors",
            joinColumns = @JoinColumn(name = "queue_id"),
            inverseJoinColumns = @JoinColumn(name = "competitor_id")
    )
    private List<Competitor> queue;

    private LocalDateTime joinedAt;
    public MatchMakingQueue() {
        this.queue = new ArrayList<Competitor>();
    }

    // -- ID --
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    // -- Queue --
    public List<Competitor> getQueue() {
        return queue;
    }
    public void setQueue(List<Competitor> queue) {
        this.queue = queue;
    }
    public void addCompetitor(Competitor competitor) {
        queue.add(competitor);
    }

    //-- Time --
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    public void setJoinedAt(LocalDateTime date) {
        this.joinedAt = date;
    }




}

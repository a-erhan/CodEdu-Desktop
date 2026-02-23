package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_making_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchMakingQueue extends BaseEntity {

    @ManyToMany
    @JoinTable(
            name = "queue_competitors",
            joinColumns = @JoinColumn(name = "queue_id"),
            inverseJoinColumns = @JoinColumn(name = "competitor_id")
    )
    @Builder.Default
    private List<Competitor> queue = new ArrayList<>();

    private LocalDateTime joinedAt;

    public boolean isQueueFull() {
        return this.queue.size() >= 2;
    }

    public void clearQueue() {
        this.queue.clear();
    }
}
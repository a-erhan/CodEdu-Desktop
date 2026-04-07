package com.codedu.models.matchmaking;

import com.codedu.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leaderboards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderBoard extends BaseEntity {

    private String name;

    private LocalDateTime lastUpdatedAt;

    @ManyToMany
    @JoinTable(
            name = "leaderboard_competitors",
            joinColumns = @JoinColumn(name = "leaderboard_id"),
            inverseJoinColumns = @JoinColumn(name = "competitor_id")
    )
    @Builder.Default
    private List<Competitor> competitors = new ArrayList<>();

    private int requiredLevel;

    public void addCompetitor(Competitor competitor) {
        if (!this.competitors.contains(competitor)) {
            this.competitors.add(competitor);
        }
    }

    public void removeCompetitor(Competitor competitor) {
        this.competitors.remove(competitor);
    }
}
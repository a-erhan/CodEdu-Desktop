package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match extends BaseEntity {

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

    public Competitor getWinner() {
        if (status != MatchStatus.FINISHED) return null;
        return (player1Time < player2Time) ? competitor1 : competitor2;
    }
    public boolean isActive() {
        return status == MatchStatus.IN_PROGRESS;
    }
}
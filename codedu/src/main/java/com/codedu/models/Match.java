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
    @JoinColumn(name = "playerOne_id")
    private Competitor playerOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerTwo_id")
    private Competitor playerTwo;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private float duration;
    private float playerOneTime;
    private float playerTwoTime;
    private int playerOneScore;
    private int playerTwoScore;
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Competitor winner;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private MatchTask task;

    public Competitor getWinner() {
        if (status != MatchStatus.ABORTED) return null;
        return (playerOneTime < playerTwoTime) ? playerOne : playerTwo;
    }
    public boolean isActive() {
        return status == MatchStatus.IN_PROGRESS;
    }

    public Competitor calculateWinner(){return null;}
}
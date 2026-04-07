package com.codedu.models.matchmaking;

import com.codedu.models.BaseEntity;
import com.codedu.models.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "competitors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(callSuper = true, exclude = "user")
public class Competitor extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private int rankingPoint;
    private int userRank;
    private int totalWins;
    private int totalLosses;
    private int totalMatches;

    public double getWinRate() {
        if (totalMatches == 0) return 0.0;
        return (double) totalWins / totalMatches * 100;
    }
}
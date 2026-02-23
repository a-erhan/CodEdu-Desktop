package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "competitors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competitor extends BaseEntity {

    @OneToOne(mappedBy = "competitor")
    private User user;

    private int rankingPoint;
    private int totalWins;
    private int totalLosses;
    private int totalMatches;

    public double getWinRate() {
        if (totalMatches == 0) return 0.0;
        return (double) totalWins / totalMatches * 100;
    }
}
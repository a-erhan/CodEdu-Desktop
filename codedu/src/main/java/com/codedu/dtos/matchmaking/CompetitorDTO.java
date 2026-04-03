package com.codedu.dtos.matchmaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorDTO {
    private int id;
    private String username;
    private int rankingPoint;
    private int userRank;
    private int totalWins;
    private int totalLosses;
    private int totalMatches;
    private double winRate;
}

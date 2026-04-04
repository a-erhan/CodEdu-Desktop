package com.codedu.dtos.matchmaking;

import lombok.Builder;

@Builder
public record CompetitorDTO(
    int id,
    String username,
    int rankingPoint,
    int userRank,
    int totalWins,
    int totalLosses,
    int totalMatches,
    double winRate
) {}

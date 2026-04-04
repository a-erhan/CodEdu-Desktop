package com.codedu.dtos.matchmaking;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record LeaderBoardDTO(
    int id,
    String name,
    int requiredLevel,
    LocalDateTime lastUpdatedAt,
    List<LeaderBoardEntryDTO> entries
) {
    @Builder
    public record LeaderBoardEntryDTO(
        int rank,
        String username,
        int rankingPoint,
        int totalWins,
        int totalMatches,
        double winRate
    ) {}
}

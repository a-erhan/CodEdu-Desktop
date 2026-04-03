package com.codedu.dtos.matchmaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderBoardDTO {
    private int id;
    private String name;
    private int requiredLevel;
    private LocalDateTime lastUpdatedAt;
    private List<LeaderBoardEntryDTO> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderBoardEntryDTO {
        private int rank;
        private String username;
        private int rankingPoint;
        private int totalWins;
        private int totalMatches;
        private double winRate;
    }
}

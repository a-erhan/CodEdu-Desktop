package com.codedu.dtos.matchmaking;

import com.codedu.models.matchmaking.MatchStatus;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MatchDTO(
    int id,
    String playerOneUsername,
    String playerTwoUsername,
    int playerOneScore,
    int playerTwoScore,
    float playerOneTime,
    float playerTwoTime,
    String winnerUsername,
    MatchStatus status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    float duration,
    int rewardXp,
    int rewardToken
) {}

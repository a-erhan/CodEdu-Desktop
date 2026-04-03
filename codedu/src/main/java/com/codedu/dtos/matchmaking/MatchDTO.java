package com.codedu.dtos.matchmaking;

import com.codedu.models.matchmaking.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private int id;
    private String playerOneUsername;
    private String playerTwoUsername;
    private int playerOneScore;
    private int playerTwoScore;
    private float playerOneTime;
    private float playerTwoTime;
    private String winnerUsername;
    private MatchStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private float duration;
    private int rewardXp;
    private int rewardToken;
}

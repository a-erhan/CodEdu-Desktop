package com.codedu.dtos.user;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserGameStateDTO(
    int id,
    int userId,
    int heartCount,
    int level,
    int xp,
    int xpToNextLevel,
    int tokenBalance,
    int currentStreak,
    LocalDateTime lastActivityDate,
    List<String> achievementNames
) {}

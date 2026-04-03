package com.codedu.dtos.gamification;

import lombok.Builder;

@Builder
public record AchievementDTO(
    int id,
    String name,
    String criteria,
    int rewardXp,
    int rewardToken,
    BadgeDTO badge
) {}

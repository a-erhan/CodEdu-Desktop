package com.codedu.dtos.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDTO {
    private int id;
    private String name;
    private String criteria;
    private int rewardXp;
    private int rewardToken;
    private BadgeDTO badge;
}

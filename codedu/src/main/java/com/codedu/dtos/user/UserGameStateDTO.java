package com.codedu.dtos.user;

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
public class UserGameStateDTO {
    private int id;
    private int userId;
    private int heartCount;
    private int level;
    private int xp;
    private int xpToNextLevel;
    private int tokenBalance;
    private int currentStreak;
    private LocalDateTime lastActivityDate;
    private List<String> achievementNames;
}

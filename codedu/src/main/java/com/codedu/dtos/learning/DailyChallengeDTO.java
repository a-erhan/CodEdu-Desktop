package com.codedu.dtos.learning;

import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

@Builder
public record DailyChallengeDTO(
    int id,
    String name,
    String description,
    LocalDate targetDate,
    int rewardXp,
    int rewardToken,
    List<QuestionDTO> questions
) {}

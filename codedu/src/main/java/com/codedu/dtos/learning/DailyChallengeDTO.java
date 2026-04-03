package com.codedu.dtos.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyChallengeDTO {
    private int id;
    private String name;
    private String description;
    private LocalDate targetDate;
    private int rewardXp;
    private int rewardToken;
    private List<QuestionDTO> questions;
}

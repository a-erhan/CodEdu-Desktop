package com.codedu.dtos.gamification;

import lombok.Builder;

@Builder
public record BadgeDTO(
    int id,
    String title,
    String description,
    String iconURL
) {}

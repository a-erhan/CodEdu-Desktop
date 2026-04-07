package com.codedu.dtos.gamification;

import com.codedu.models.gamification.Achievement;

/**
 * Precomputed achievement progress for UI rendering off the JavaFX thread.
 */
public record AchievementProgressSnapshot(Achievement achievement, double progress, String progressText) {
}

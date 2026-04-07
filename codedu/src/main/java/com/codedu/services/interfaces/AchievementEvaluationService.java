package com.codedu.services.interfaces;

import com.codedu.dtos.gamification.AchievementProgressSnapshot;
import com.codedu.models.gamification.Achievement;
import com.codedu.models.user.User;

import java.util.List;

public interface AchievementEvaluationService {

    double getProgressPercentage(Achievement achievement, User user);

    String getProgressText(Achievement achievement, User user);

    /**
     * Loads all achievements and progress for a user in one transactional read (safe to call from a worker thread).
     */
    List<AchievementProgressSnapshot> loadAllProgressSnapshots(int userId);
}

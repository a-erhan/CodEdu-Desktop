package com.codedu.services.interfaces;

import com.codedu.models.gamification.Achievement;
import com.codedu.models.user.User;

public interface AchievementEvaluationService {

    double getProgressPercentage(Achievement achievement, User user);

    String getProgressText(Achievement achievement, User user);
}

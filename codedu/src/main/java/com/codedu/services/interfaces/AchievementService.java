package com.codedu.services.interfaces;

import com.codedu.models.user.User;

public interface AchievementService {
    /**
     * Scans all available achievements and awards them to the user if criteria are met.
     */
    void checkAndAwardAchievements(User user);
}

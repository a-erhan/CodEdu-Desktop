package com.codedu.services.interfaces;

import com.codedu.dtos.learning.DailyChallengeDTO;
import com.codedu.models.learning.DailyChallenge;

public interface DailyChallengeService {

    DailyChallengeDTO getTodaysChallenge();

    /** Entity version for controllers still using entity classes */
    DailyChallenge getTodaysChallengeEntity();

    String submitDailyChallenge(int userId, int correctAnswers);
}

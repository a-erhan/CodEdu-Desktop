package com.codedu.services.interfaces;

import com.codedu.models.learning.DailyChallenge;
import com.codedu.models.user.UserGameState;

public interface DailyChallengeService {

    DailyChallenge getTodaysChallenge();

    String submitDailyChallenge(UserGameState gameState, int correctAnswers);
}

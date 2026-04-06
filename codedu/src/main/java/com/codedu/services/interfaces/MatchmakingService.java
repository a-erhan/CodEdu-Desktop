package com.codedu.services.interfaces;

import com.codedu.models.user.User;

import com.codedu.models.matchmaking.MatchResult;

public interface MatchmakingService {

    void joinQueue(User user);

    void leaveQueue(int userId);

    void reportWin(MatchResult result);

    void broadcastAttempt(com.codedu.models.matchmaking.MatchAttemptUpdate update);
}

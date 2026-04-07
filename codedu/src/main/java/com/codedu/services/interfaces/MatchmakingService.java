package com.codedu.services.interfaces;

import com.codedu.models.user.User;
import com.codedu.models.matchmaking.MatchResult;
import com.codedu.models.matchmaking.MatchAttemptUpdate;

public interface MatchmakingService {

    void joinQueue(User user, String sessionId);

    void leaveQueue(int userId);

    void handleDisconnect(String sessionId);

    void reportWin(MatchResult result);

    void broadcastAttempt(MatchAttemptUpdate update);
}

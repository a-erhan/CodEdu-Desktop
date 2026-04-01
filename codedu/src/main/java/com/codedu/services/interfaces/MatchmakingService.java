package com.codedu.services.interfaces;

import com.codedu.models.user.User;

public interface MatchmakingService {

    void joinQueue(User user);

    void leaveQueue(int userId);
}

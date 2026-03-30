package com.codedu.services.interfaces;

import com.codedu.models.matchmaking.LeaderBoard;

public interface LeaderBoardService {

    LeaderBoard getLeaderboardByName(String name);
}

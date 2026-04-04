package com.codedu.services.interfaces;

import com.codedu.dtos.matchmaking.LeaderBoardDTO;
import com.codedu.models.matchmaking.LeaderBoard;

public interface LeaderBoardService {

    LeaderBoardDTO getLeaderboardByName(String name);

    /** Entity version for controllers still using entity classes */
    LeaderBoard getLeaderboardEntityByName(String name);
}

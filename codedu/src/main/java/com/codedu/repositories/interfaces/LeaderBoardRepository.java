package com.codedu.repositories.interfaces;

import com.codedu.models.matchmaking.LeaderBoard;

import java.util.Optional;

public interface LeaderBoardRepository extends GenericRepository<LeaderBoard> {
    Optional<LeaderBoard> findByName(String name);
}

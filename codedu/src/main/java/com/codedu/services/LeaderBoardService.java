package com.codedu.services;

import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.repositories.interfaces.LeaderBoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LeaderBoardService {

    private final LeaderBoardRepository leaderBoardRepository;

    public LeaderBoardService(LeaderBoardRepository leaderBoardRepository) {
        this.leaderBoardRepository = leaderBoardRepository;
    }

    /**
     * Retrieves the leaderboard by its exact name.
     * Returns an empty/dummy leaderboard if not found in the database yet.
     */
    @Transactional(readOnly = true)
    public LeaderBoard getLeaderboardByName(String name) {
        Optional<LeaderBoard> leaderboardOpt = leaderBoardRepository.findByName(name);

        if (leaderboardOpt.isPresent()) {
            return leaderboardOpt.get();
        } else {
            // Prevent UI crash if the database is empty during development
            System.out.println("Leaderboard '" + name + "' not found in DB. Returning an empty template.");
            return LeaderBoard.builder()
                    .name(name)
                    .requiredLevel(1)
                    .userRank(0)
                    .build();
        }
    }
}
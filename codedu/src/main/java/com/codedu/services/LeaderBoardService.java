package com.codedu.services;

import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.CompetitorRepository;
import com.codedu.repositories.interfaces.LeaderBoardRepository;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeaderBoardService {

    private final LeaderBoardRepository leaderBoardRepository;
    private final UserRepository userRepository;
    private final CompetitorRepository competitorRepository;

    public LeaderBoardService(LeaderBoardRepository leaderBoardRepository,
            UserRepository userRepository,
            CompetitorRepository competitorRepository) {
        this.leaderBoardRepository = leaderBoardRepository;
        this.userRepository = userRepository;
        this.competitorRepository = competitorRepository;
    }

    /**
     * Retrieves the leaderboard by its exact name.
     * Returns an empty/dummy leaderboard if not found in the database yet.
     */
    @Transactional
    public LeaderBoard getLeaderboardByName(String name) {
        Optional<LeaderBoard> leaderboardOpt = leaderBoardRepository.findByName(name);

        if (leaderboardOpt.isPresent()) {
            LeaderBoard lb = leaderboardOpt.get();
            if (lb.getCompetitors() != null) {
                lb.getCompetitors().size(); // Force initialization
            }
            return lb;
        } else {
            // Automatically create and populate the leaderboard if it doesn't exist
            System.out.println("Leaderboard '" + name + "' not found in DB. Creating and populating...");

            LeaderBoard newLeaderBoard = LeaderBoard.builder()
                    .name(name)
                    .requiredLevel(1)
                    .lastUpdatedAt(LocalDateTime.now())
                    .build();

            List<User> allUsers = userRepository.getAll();
            List<Competitor> allCompetitors = new ArrayList<>();

            for (User u : allUsers) {
                Competitor c = u.getCompetitor();
                if (c == null) {
                    c = Competitor.builder()
                            .user(u)
                            .rankingPoint(u.getGameState() != null ? u.getGameState().getXp() : 0)
                            .totalWins(0)
                            .totalLosses(0)
                            .totalMatches(0)
                            .build();
                    competitorRepository.save(c);
                    u.setCompetitor(c);
                    userRepository.update(u);
                } else if (u.getGameState() != null) {
                    // Sync XP to ranking points for now
                    c.setRankingPoint(u.getGameState().getXp());
                    competitorRepository.update(c);
                }
                allCompetitors.add(c);
            }

            // Sort competitors by ranking points descending
            allCompetitors.sort((c1, c2) -> Integer.compare(c2.getRankingPoint(), c1.getRankingPoint()));

            // Assign ranks
            for (int i = 0; i < allCompetitors.size(); i++) {
                allCompetitors.get(i).setUserRank(i + 1);
                competitorRepository.update(allCompetitors.get(i));
                newLeaderBoard.addCompetitor(allCompetitors.get(i));
            }

            leaderBoardRepository.save(newLeaderBoard);
            return newLeaderBoard;
        }
    }
}
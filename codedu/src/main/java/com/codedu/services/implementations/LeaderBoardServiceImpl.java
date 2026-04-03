package com.codedu.services.implementations;

import com.codedu.dtos.matchmaking.LeaderBoardDTO;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.services.interfaces.LeaderBoardService;
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
import java.util.stream.Collectors;

@Service
public class LeaderBoardServiceImpl implements LeaderBoardService {

    private final LeaderBoardRepository leaderBoardRepository;
    private final UserRepository userRepository;
    private final CompetitorRepository competitorRepository;

    public LeaderBoardServiceImpl(LeaderBoardRepository leaderBoardRepository,
            UserRepository userRepository,
            CompetitorRepository competitorRepository) {
        this.leaderBoardRepository = leaderBoardRepository;
        this.userRepository = userRepository;
        this.competitorRepository = competitorRepository;
    }

    @Transactional
    public LeaderBoardDTO getLeaderboardByName(String name) {
        Optional<LeaderBoard> leaderboardOpt = leaderBoardRepository.findByName(name);

        LeaderBoard lb;
        if (leaderboardOpt.isPresent()) {
            lb = leaderboardOpt.get();
            if (lb.getCompetitors() != null) {
                lb.getCompetitors().size(); // Force initialization
            }
        } else {
            System.out.println("Leaderboard '" + name + "' not found in DB. Creating and populating...");

            lb = LeaderBoard.builder()
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
                    c.setRankingPoint(u.getGameState().getXp());
                    competitorRepository.update(c);
                }
                allCompetitors.add(c);
            }

            allCompetitors.sort((c1, c2) -> Integer.compare(c2.getRankingPoint(), c1.getRankingPoint()));

            for (int i = 0; i < allCompetitors.size(); i++) {
                allCompetitors.get(i).setUserRank(i + 1);
                competitorRepository.update(allCompetitors.get(i));
                lb.addCompetitor(allCompetitors.get(i));
            }

            leaderBoardRepository.save(lb);
        }

        return toDTO(lb);
    }

    private LeaderBoardDTO toDTO(LeaderBoard lb) {
        List<LeaderBoardDTO.LeaderBoardEntryDTO> entries = lb.getCompetitors() != null
                ? lb.getCompetitors().stream()
                        .map(c -> LeaderBoardDTO.LeaderBoardEntryDTO.builder()
                                .rank(c.getUserRank())
                                .username(c.getUser() != null ? c.getUser().getUsername() : "Unknown")
                                .rankingPoint(c.getRankingPoint())
                                .totalWins(c.getTotalWins())
                                .totalMatches(c.getTotalMatches())
                                .winRate(c.getWinRate())
                                .build())
                        .collect(Collectors.toList())
                : List.of();

        return LeaderBoardDTO.builder()
                .id(lb.getId())
                .name(lb.getName())
                .requiredLevel(lb.getRequiredLevel())
                .lastUpdatedAt(lb.getLastUpdatedAt())
                .entries(entries)
                .build();
    }

    @Override
    @Transactional
    public LeaderBoard getLeaderboardEntityByName(String name) {
        Optional<LeaderBoard> leaderboardOpt = leaderBoardRepository.findByName(name);
        if (leaderboardOpt.isPresent()) {
            LeaderBoard lb = leaderboardOpt.get();
            if (lb.getCompetitors() != null) {
                lb.getCompetitors().size();
            }
            return lb;
        }
        getLeaderboardByName(name);
        return leaderBoardRepository.findByName(name).orElse(null);
    }
}
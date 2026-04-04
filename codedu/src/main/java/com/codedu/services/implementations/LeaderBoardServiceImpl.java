package com.codedu.services.implementations;

import com.codedu.dtos.matchmaking.LeaderBoardDTO;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.CompetitorRepository;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.LeaderBoardService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaderBoardServiceImpl implements LeaderBoardService {

    private final UserRepository userRepository;
    private final CompetitorRepository competitorRepository;

    public LeaderBoardServiceImpl(UserRepository userRepository,
            CompetitorRepository competitorRepository) {
        this.userRepository = userRepository;
        this.competitorRepository = competitorRepository;
    }

    @Override
    @Transactional
    public LeaderBoardDTO getLeaderboardByName(String name) {
        LeaderBoard lb = getLeaderboardEntityByName(name);
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
        List<User> users = userRepository.findAllActiveWithCompetitorAndGameState();
        List<User> scoped = filterByScope(users, name);
        List<Competitor> rows = new ArrayList<>();
        for (User u : scoped) {
            ensureCompetitor(u);
            Competitor c = u.getCompetitor();
            c.setRankingPoint(progressionScore(u));
            c.setUserRank(0);
            rows.add(c);
        }
        rows.sort(Comparator.comparingInt(Competitor::getRankingPoint).reversed());
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setUserRank(i + 1);
        }
        for (Competitor c : rows) {
            if (c.getUser() != null) {
                Hibernate.initialize(c.getUser());
                if (c.getUser().getGameState() != null) {
                    Hibernate.initialize(c.getUser().getGameState());
                }
            }
        }
        return LeaderBoard.builder()
                .name(name != null && !name.isBlank() ? name : "All-Time")
                .lastUpdatedAt(LocalDateTime.now())
                .competitors(rows)
                .build();
    }

    private static List<User> filterByScope(List<User> all, String scope) {
        if (scope == null || scope.isBlank() || "All-Time".equalsIgnoreCase(scope.trim())) {
            return new ArrayList<>(all);
        }
        LocalDateTime cutoff = LocalDateTime.now();
        String s = scope.trim();
        if ("Weekly".equalsIgnoreCase(s)) {
            cutoff = cutoff.minusWeeks(1);
        } else if ("Monthly".equalsIgnoreCase(s)) {
            cutoff = cutoff.minusMonths(1);
        } else {
            return new ArrayList<>(all);
        }
        final LocalDateTime c = cutoff;
        return all.stream()
                .filter(u -> {
                    UserGameState gs = u.getGameState();
                    if (gs == null || gs.getLastActivityDate() == null) {
                        return false;
                    }
                    return !gs.getLastActivityDate().isBefore(c);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static int progressionScore(User u) {
        UserGameState gs = u.getGameState();
        if (gs == null) {
            return 0;
        }
        return gs.getLevel() * 1_000_000 + gs.getXp();
    }

    private void ensureCompetitor(User u) {
        if (u.getCompetitor() != null) {
            return;
        }
        Competitor c = Competitor.builder()
                .user(u)
                .rankingPoint(progressionScore(u))
                .totalWins(0)
                .totalLosses(0)
                .totalMatches(0)
                .build();
        competitorRepository.save(c);
        u.setCompetitor(c);
        userRepository.update(u);
    }
}

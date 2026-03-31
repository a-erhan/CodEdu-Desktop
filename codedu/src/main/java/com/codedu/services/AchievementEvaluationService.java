package com.codedu.services;

import com.codedu.models.gamification.Achievement;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.AchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Optional;

@Service
public class AchievementEvaluationService {

    private final UserService userService;
    private final AchievementRepository achievementRepository;

    public AchievementEvaluationService(UserService userService, AchievementRepository achievementRepository) {
        this.userService = userService;
        this.achievementRepository = achievementRepository;
    }

    // Only called silently on login/profile load to fix corrupted accounts (0 XP
    // but owns achievements)
    @Transactional
    public void fixMissingXPOnLogin(User detachedUser) {
        if (detachedUser == null)
            return;

        Optional<User> uOpt = userService.getUserById(detachedUser.getId());
        if (uOpt.isEmpty())
            return;
        User user = uOpt.get();

        UserGameState state = user.getGameState();
        if (state == null)
            return;

        // Force initialization
        if (state.getAchievements() != null) {
            state.getAchievements().size();
        }

        // --- Fail-safe bug fix logic ---
        if (state.getAchievements() != null && !state.getAchievements().isEmpty()
                && state.getXp() == 0 && state.getTokenBalance() == 0) {
            System.out.println("Retroactively fixing missing XP/Tokens for user: " + user.getUsername());
            for (Achievement earned : state.getAchievements()) {
                if (earned.getReward() != null) {
                    state.setTokenBalance(state.getTokenBalance() + earned.getReward().getToken());
                    state.setXp(state.getXp() + earned.getReward().getXp());
                    while (state.getXp() >= state.getLevel() * 1000) {
                        state.setXp(state.getXp() - state.getLevel() * 1000);
                        state.setLevel(state.getLevel() + 1);
                    }
                }
            }
            userService.saveUser(user);
        }

        detachedUser.setGameState(state);
        if (detachedUser.getGameState().getAchievements() != null) {
            detachedUser.getGameState().getAchievements().size();
        }
    }

    @Transactional
    public boolean claimAchievement(User detachedUser, int achievementId) {
        if (detachedUser == null)
            return false;

        Optional<User> uOpt = userService.getUserById(detachedUser.getId());
        if (uOpt.isEmpty())
            return false;
        User user = uOpt.get();

        UserGameState state = user.getGameState();
        if (state == null)
            return false;

        Optional<Achievement> aOpt = achievementRepository.findById(achievementId);
        if (aOpt.isEmpty())
            return false;
        Achievement a = aOpt.get();

        if (state.getAchievements() != null) {
            state.getAchievements().size();
            for (Achievement earned : state.getAchievements()) {
                if (earned.getBadge() != null)
                    Hibernate.initialize(earned.getBadge());
                if (earned.getId() == a.getId())
                    return false; // Already claimed
            }
        }

        if (getProgressPercentage(a, user) >= 1.0) {
            if (a.getBadge() != null)
                Hibernate.initialize(a.getBadge());
            state.getAchievements().add(a);
            if (a.getReward() != null) {
                state.setTokenBalance(state.getTokenBalance() + a.getReward().getToken());
                state.setXp(state.getXp() + a.getReward().getXp());
                while (state.getXp() >= state.getLevel() * 1000) {
                    state.setXp(state.getXp() - state.getLevel() * 1000);
                    state.setLevel(state.getLevel() + 1);
                }
            }
            userService.saveUser(user);
            detachedUser.setGameState(state);
            if (detachedUser.getGameState().getAchievements() != null) {
                detachedUser.getGameState().getAchievements().size(); // sync proxy
            }
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Achievement> getAllAchievementsWithBadges() {
        List<Achievement> achievements = achievementRepository.getAll();
        for (Achievement a : achievements) {
            if (a.getBadge() != null) {
                Hibernate.initialize(a.getBadge()); // Force deep initialization
            }
        }
        return achievements;
    }

    @Transactional(readOnly = true)
    public List<Achievement> getUserAchievementsWithBadges(User detachedUser) {
        if (detachedUser == null)
            return new java.util.ArrayList<>();
        Optional<User> uOpt = userService.getUserById(detachedUser.getId());
        if (uOpt.isEmpty())
            return new java.util.ArrayList<>();

        User user = uOpt.get();
        if (user.getGameState() == null || user.getGameState().getAchievements() == null) {
            return new java.util.ArrayList<>();
        }

        List<Achievement> achievements = user.getGameState().getAchievements();
        achievements.size(); // Init list
        for (Achievement a : achievements) {
            if (a.getBadge() != null) {
                Hibernate.initialize(a.getBadge());
            }
        }
        return achievements;
    }

    public double getProgressPercentage(Achievement achievement, User user) {
        if (user == null || achievement == null)
            return 0.0;

        UserGameState state = user.getGameState();
        Competitor comp = user.getCompetitor();

        // If earned already, return 100%
        if (state != null && state.getAchievements() != null) {
            for (Achievement earned : state.getAchievements()) {
                if (earned.getId() == achievement.getId())
                    return 1.0;
            }
        }

        String name = achievement.getName();
        double progress = 0.0;

        switch (name) {
            case "First Blood":
                progress = (comp != null) ? Math.min(1.0, comp.getTotalWins() / 1.0) : 0;
                break;
            case "Veteran":
                progress = (comp != null) ? Math.min(1.0, comp.getTotalWins() / 5.0) : 0;
                break;
            case "Gladiator":
                progress = (comp != null) ? Math.min(1.0, comp.getTotalWins() / 10.0) : 0;
                break;
            case "Scholar":
                progress = (comp != null) ? Math.min(1.0, comp.getTotalMatches() / 5.0) : 0;
                break;
            case "Professor":
                progress = (comp != null) ? Math.min(1.0, comp.getTotalMatches() / 15.0) : 0;
                break;
            case "Rich":
                progress = (state != null) ? Math.min(1.0, state.getTokenBalance() / 1000.0) : 0;
                break;
            case "Tycoon":
                progress = (state != null) ? Math.min(1.0, state.getTokenBalance() / 5000.0) : 0;
                break;
            case "Dedicated":
                progress = (state != null) ? Math.min(1.0, state.getLevel() / 5.0) : 0;
                break;
            case "Master":
                progress = (state != null) ? Math.min(1.0, state.getLevel() / 10.0) : 0;
                break;
            case "Social Butterfly":
                int friends = userService.getAcceptedFriends(user).size();
                progress = Math.min(1.0, friends / 3.0);
                break;
            default:
                progress = 0.0;
        }

        return progress;
    }

    public String getProgressText(Achievement achievement, User user) {
        UserGameState state = user != null ? user.getGameState() : null;
        Competitor comp = user != null ? user.getCompetitor() : null;
        String name = achievement.getName();

        switch (name) {
            case "First Blood":
                return (comp != null ? comp.getTotalWins() : 0) + " / 1 Wins";
            case "Veteran":
                return (comp != null ? comp.getTotalWins() : 0) + " / 5 Wins";
            case "Gladiator":
                return (comp != null ? comp.getTotalWins() : 0) + " / 10 Wins";
            case "Scholar":
                return (comp != null ? comp.getTotalMatches() : 0) + " / 5 Matches";
            case "Professor":
                return (comp != null ? comp.getTotalMatches() : 0) + " / 15 Matches";
            case "Rich":
                return (state != null ? state.getTokenBalance() : 0) + " / 1000 Tokens";
            case "Tycoon":
                return (state != null ? state.getTokenBalance() : 0) + " / 5000 Tokens";
            case "Dedicated":
                return (state != null ? state.getLevel() : 1) + " / 5 Level";
            case "Master":
                return (state != null ? state.getLevel() : 1) + " / 10 Level";
            case "Social Butterfly":
                return (user != null ? userService.getAcceptedFriends(user).size() : 0) + " / 3 Friends";
            default:
                return "0 / 1";
        }
    }
}

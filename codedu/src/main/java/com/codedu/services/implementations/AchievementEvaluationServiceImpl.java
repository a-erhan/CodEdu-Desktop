package com.codedu.services.implementations;

import com.codedu.models.gamification.Achievement;
import com.codedu.services.interfaces.AchievementEvaluationService;
import com.codedu.services.interfaces.UserService;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import org.springframework.stereotype.Service;

@Service
public class AchievementEvaluationServiceImpl implements AchievementEvaluationService {

    private final UserService userService;

    public AchievementEvaluationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    public double getProgressPercentage(Achievement achievement, User user) {
        if (user == null || achievement == null)
            return 0.0;

        UserGameState state = user.getGameState();
        Competitor comp = user.getCompetitor();

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
                int friends = userService.getAcceptedFriends(user.getId()).size();
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
                return (user != null ? userService.getAcceptedFriends(user.getId()).size() : 0) + " / 3 Friends";
            default:
                return "0 / 1";
        }
    }
}

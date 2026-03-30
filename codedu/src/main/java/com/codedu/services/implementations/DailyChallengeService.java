package com.codedu.services.implementations;

import com.codedu.models.learning.DailyChallenge;
import com.codedu.models.learning.Question;
import com.codedu.models.learning.Reward;
import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.DailyChallengeRepository;
import com.codedu.repositories.interfaces.QuestionRepository;
import com.codedu.repositories.interfaces.UserGameStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DailyChallengeService {

    private DailyChallengeRepository dailyChallengeRepository;
    private QuestionRepository questionRepository;
    private UserGameStateRepository userGameStateRepository;

    public DailyChallengeService(DailyChallengeRepository dailyChallengeRepository,
            QuestionRepository questionRepository,
            UserGameStateRepository userGameStateRepository) {
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.questionRepository = questionRepository;
        this.userGameStateRepository = userGameStateRepository;
    }

    @Transactional
    public DailyChallenge getTodaysChallenge() {
        LocalDate today = LocalDate.now();

        Optional<DailyChallenge> existingChallenge = dailyChallengeRepository.findByTargetDate(today);
        if (existingChallenge.isPresent()) {
            DailyChallenge dc = existingChallenge.get();
            if (dc.getQuestions() != null) {
                dc.getQuestions().size(); // Force initialization
            }
            return dc;
        }

        List<Question> todaysQuestions = questionRepository.getRandomQuestions(3);

        if (todaysQuestions.isEmpty()) {
            System.err.println("[DailyChallengeService] No questions found in DB to create a challenge!");
            return null; // Controller will handle this
        }

        System.out.println(
                "[DailyChallengeService] Creating new challenge with " + todaysQuestions.size() + " questions.");

        Reward dailyReward = new Reward();
        dailyReward.setXp(50);

        DailyChallenge newChallenge = DailyChallenge.builder()
                .name("Daily Challenge - " + today)
                .description("Complete these questions to earn the reward!")
                .targetDate(today)
                .questions(todaysQuestions)
                .reward(dailyReward)
                .build();

        dailyChallengeRepository.save(newChallenge);

        return newChallenge;
    }

    /**
     * Called when the user submits their daily challenge answers.
     */
    @Transactional
    public String submitDailyChallenge(UserGameState gameState, int correctAnswers) {
        if (correctAnswers <= 0) {
            // Optional: Decrease heart count here if they fail completely
            return "No correct answers. Better luck tomorrow!";
        }

        // 1. Get today's challenge and total potential XP
        DailyChallenge todaysChallenge = getTodaysChallenge();
        int totalPotentialXp = todaysChallenge.getReward().getXp();

        // 2. Calculate earned XP based on the number of correct answers (out of 3)
        int earnedXp = (totalPotentialXp * correctAnswers) / 3;

        // 3. Add XP to the user's game state
        gameState.setXp(gameState.getXp() + earnedXp);

        // 4. Handle Level Up logic (using the existing getXpToNextLevel method)
        while (gameState.getXp() >= gameState.getXpToNextLevel()) {
            gameState.setLevel(gameState.getLevel() + 1);
            gameState.setHeartCount(5); // Refill hearts as a level-up reward
        }

        // 5. Increase streak only if they got a perfect score (3/3)
        if (correctAnswers == 3) {
            gameState.setCurrentStreak(gameState.getCurrentStreak() + 1);
        }

        // 6. Update last activity timestamp
        gameState.setLastActivityDate(LocalDateTime.now());

        // 7. Save the updated state to the database using the new repository
        userGameStateRepository.update(gameState);

        return "Congratulations! You earned " + earnedXp + " XP for " + correctAnswers + " correct answers!";
    }
}

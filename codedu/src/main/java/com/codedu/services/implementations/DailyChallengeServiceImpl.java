package com.codedu.services.implementations;

import com.codedu.dtos.learning.DailyChallengeDTO;
import com.codedu.dtos.learning.QuestionDTO;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.DailyChallenge;
import com.codedu.models.learning.MultipleChoiceQuestion;
import com.codedu.services.interfaces.DailyChallengeService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailyChallengeServiceImpl implements DailyChallengeService {

    private DailyChallengeRepository dailyChallengeRepository;
    private QuestionRepository questionRepository;
    private UserGameStateRepository userGameStateRepository;

    public DailyChallengeServiceImpl(DailyChallengeRepository dailyChallengeRepository,
            QuestionRepository questionRepository,
            UserGameStateRepository userGameStateRepository) {
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.questionRepository = questionRepository;
        this.userGameStateRepository = userGameStateRepository;
    }

    @Transactional
    public DailyChallengeDTO getTodaysChallenge() {
        LocalDate today = LocalDate.now();

        Optional<DailyChallenge> existingChallenge = dailyChallengeRepository.findByTargetDate(today);
        if (existingChallenge.isPresent()) {
            DailyChallenge dc = existingChallenge.get();
            if (dc.getQuestions() != null) {
                dc.getQuestions().size(); // Force initialization
            }
            return toDTO(dc);
        }

        List<Question> todaysQuestions = questionRepository.getRandomQuestions(3);

        if (todaysQuestions.isEmpty()) {
            System.err.println("[DailyChallengeService] No questions found in DB to create a challenge!");
            return null;
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

        return toDTO(newChallenge);
    }

    @Transactional
    public String submitDailyChallenge(int userId, int correctAnswers) {
        if (correctAnswers <= 0) {
            return "No correct answers. Better luck tomorrow!";
        }

        UserGameState gameState = userGameStateRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No game state for user id " + userId));

        DailyChallenge todaysChallenge = dailyChallengeRepository.findByTargetDate(LocalDate.now()).orElse(null);
        if (todaysChallenge == null) return "No challenge available today.";

        int totalPotentialXp = todaysChallenge.getReward().getXp();
        int earnedXp = (totalPotentialXp * correctAnswers) / 3;
        earnedXp = gameState.withDoubleXpApplied(earnedXp);

        gameState.setXp(gameState.getXp() + earnedXp);

        while (gameState.getXp() >= gameState.getXpToNextLevel()) {
            gameState.setLevel(gameState.getLevel() + 1);
            gameState.setHeartCount(UserGameState.MAX_HEARTS);
        }

        if (correctAnswers == 3) {
            gameState.setCurrentStreak(gameState.getCurrentStreak() + 1);
        }

        gameState.setLastActivityDate(LocalDateTime.now());
        userGameStateRepository.update(gameState);

        return "Congratulations! You earned " + earnedXp + " XP for " + correctAnswers + " correct answers!";
    }

    private DailyChallengeDTO toDTO(DailyChallenge dc) {
        List<QuestionDTO> questionDTOs = dc.getQuestions() != null
                ? dc.getQuestions().stream().map(this::questionToDTO).collect(Collectors.toList())
                : Collections.emptyList();

        return DailyChallengeDTO.builder()
                .id(dc.getId())
                .name(dc.getName())
                .description(dc.getDescription())
                .targetDate(dc.getTargetDate())
                .rewardXp(dc.getReward() != null ? dc.getReward().getXp() : 0)
                .rewardToken(dc.getReward() != null ? dc.getReward().getToken() : 0)
                .questions(questionDTOs)
                .build();
    }

    private QuestionDTO questionToDTO(Question q) {
        return QuestionDTO.builder()
                .id(q.getId())
                .title(q.getTitle())
                .content(q.getContent())
                .hint(q.getHint())
                .questionType(q.getQuestionType())
                .questionDifficulty(q.getQuestionDifficulty())
                .rewardXp(q.getReward() != null ? q.getReward().getXp() : 0)
                .rewardToken(q.getReward() != null ? q.getReward().getToken() : 0)
                .boilerplateCode(q instanceof CodeImplementationQuestion ciq ? ciq.getBoilerplateCode() : null)
                .testCases(null)
                .choices(q instanceof MultipleChoiceQuestion mcq ? mcq.getChoices() : null)
                .build();
    }
    @Override
    @Transactional
    public DailyChallenge getTodaysChallengeEntity() {
        LocalDate today = LocalDate.now();
        Optional<DailyChallenge> existingChallenge = dailyChallengeRepository.findByTargetDate(today);
        if (existingChallenge.isPresent()) {
            DailyChallenge dc = existingChallenge.get();
            if (dc.getQuestions() != null) {
                dc.getQuestions().size();
            }
            return dc;
        }
        // Trigger creation via DTO method, then re-fetch
        getTodaysChallenge();
        return dailyChallengeRepository.findByTargetDate(today).orElse(null);
    }
}

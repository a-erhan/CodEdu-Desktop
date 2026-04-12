package com.codedu.seeders;

import com.codedu.models.gamification.Achievement;
import com.codedu.models.gamification.Badge;
import com.codedu.models.learning.Reward;
import com.codedu.repositories.interfaces.AchievementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class GamificationSeeder implements CommandLineRunner {

    private final AchievementRepository achievementRepository;
    private final TransactionTemplate transactionTemplate;

    public GamificationSeeder(AchievementRepository achievementRepository,
                              PlatformTransactionManager transactionManager) {
        this.achievementRepository = achievementRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(String... args) {
        transactionTemplate.executeWithoutResult(status -> {
            if (achievementRepository.getAll().size() >= 10) {
                System.out.println(">>> [Seeder] Achievements already exist. Skipping gamification seeding.");
                return;
            }

            System.out.println(">>> [Seeder] Starting Gamification Seeding...");

            createAchievement("First Blood", "Win your first 1v1 match.", 50, 5, "first_blood.png");
            createAchievement("Veteran", "Win 5 matchmaking games.", 200, 20, "veteran.png");
            createAchievement("Gladiator", "Win 10 matchmaking games.", 500, 50, "gladiator.png");
            createAchievement("Scholar", "Complete 5 daily challenges.", 100, 10, "scholar.png");
            createAchievement("Professor", "Complete 15 daily challenges.", 400, 40, "professor.png");
            createAchievement("Rich", "Accumulate 1000 tokens.", 0, 0, "rich.png");
            createAchievement("Tycoon", "Accumulate 5000 tokens.", 0, 0, "tycoon.png");
            createAchievement("Dedicated", "Reach Level 5.", 500, 50, "dedicated.png");
            createAchievement("Master", "Reach Level 10.", 1500, 150, "master.png");
            createAchievement("Social Butterfly", "Have 3 accepted friends.", 150, 15, "social_butterfly.png");

            System.out.println(">>> [Seeder] Gamification Seeding Completed!");
        });
    }

    private void createAchievement(String name, String criteria, int rewardXp, int rewardTokens, String icon) {
        Reward reward = new Reward();
        reward.setXp(rewardXp);
        reward.setToken(rewardTokens);

        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setCriteria(criteria);
        achievement.setReward(reward);

        Badge badge = new Badge();
        badge.setTitle(name);
        badge.setDescription(criteria);

        badge.setIconURL("/com/codedu/images/badges/" + icon);

        achievement.setBadge(badge);
        badge.setAchievement(achievement);

        achievementRepository.save(achievement);
    }
}

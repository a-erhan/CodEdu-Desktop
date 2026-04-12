package com.codedu.repositories.interfaces;

import com.codedu.models.gamification.Achievement;

import java.util.Optional;

public interface AchievementRepository extends GenericRepository<Achievement> {

    Optional<Achievement> findByName(String name);

}

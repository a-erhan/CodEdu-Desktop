package com.codedu.repositories.interfaces;

import com.codedu.models.DailyChallenge;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyChallengeRepository extends GenericRepository<DailyChallenge> {
    Optional<DailyChallenge> findByName(String name);
    Optional<DailyChallenge> findByTargetDate(LocalDate date);
}

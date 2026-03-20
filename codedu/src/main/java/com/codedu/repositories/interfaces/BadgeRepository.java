package com.codedu.repositories.interfaces;

import com.codedu.models.gamification.Badge;

import java.util.Optional;

public interface BadgeRepository extends GenericRepository<Badge> {
    Optional<Badge> findByTitle(String title);
}

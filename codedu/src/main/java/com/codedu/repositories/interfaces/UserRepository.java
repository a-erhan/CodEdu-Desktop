package com.codedu.repositories.interfaces;

import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends GenericRepository<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameWithAchievements(String username);
    boolean existsByUsername(String username);

    Optional<User> findByIdWithInventoryAndGameState(int id);

    Optional<User> findByUsernameWithInventoryAndGameState(String username);

    List<User> findAllActiveWithCompetitorAndGameState();

    Optional<User> findByIdWithGameStateAndAchievements(int id);
}

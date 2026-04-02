package com.codedu.repositories.interfaces;

import com.codedu.models.user.User;

import java.util.Optional;

public interface UserRepository extends GenericRepository<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameWithAchievements(String username);
    boolean existsByUsername(String username);

    /**
     * Load user with gameState and inventory (including inventory items + item) for store/inventory operations.
     */
    Optional<User> findByIdWithInventoryAndGameState(int id);
}

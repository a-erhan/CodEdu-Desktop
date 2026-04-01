package com.codedu.services.interfaces;

import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;

import java.util.Optional;

/**
 * Persistence and core field updates for {@link UserGameState} (load by user, create default row,
 * adjust XP/tokens). Feature-specific rules belong in their own services (e.g. daily challenge, store).
 */
public interface UserGameStateService {

    /**
     * Ensures the user has a persisted game state row; refreshes {@link User#setGameState} from the database when present,
     * or creates defaults and persists via the user aggregate.
     */
    void ensureGameStateForUser(User user);

    /**
     * Reloads game state from the database onto the given user (for UI after a transactional update).
     */
    void refreshGameStateOnUser(User user);

    Optional<UserGameState> findByUserId(int userId);

    void addXpAndTokens(int userId, int xpDelta, int tokenDelta);

    /**
     * @return false if missing state or insufficient tokens
     */
    boolean tryDeductTokens(int userId, int amount);
}

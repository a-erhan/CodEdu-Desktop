package com.codedu.services.interfaces;

import com.codedu.dtos.user.UserGameStateDTO;
import com.codedu.models.user.User;

import java.util.Optional;

public interface UserGameStateService {

    void ensureGameStateForUser(User user);

    void refreshGameStateOnUser(User user);

    Optional<UserGameStateDTO> findByUserId(int userId);

    void addXpAndTokens(int userId, int xpDelta, int tokenDelta);

    boolean tryDeductTokens(int userId, int amount);
}

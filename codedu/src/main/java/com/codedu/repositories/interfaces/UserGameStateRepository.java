package com.codedu.repositories.interfaces;

import com.codedu.models.user.UserGameState;

import java.util.Optional;

public interface UserGameStateRepository extends GenericRepository<UserGameState> {
    Optional<UserGameState> findByUserId(int userId);
}

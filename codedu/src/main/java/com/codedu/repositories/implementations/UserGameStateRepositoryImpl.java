package com.codedu.repositories.implementations;

import com.codedu.models.UserGameState;
import com.codedu.repositories.interfaces.UserGameStateRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserGameStateRepositoryImpl extends GenericRepositoryImpl<UserGameState> implements UserGameStateRepository {

    public UserGameStateRepositoryImpl() {
        super(UserGameState.class);
    }
}

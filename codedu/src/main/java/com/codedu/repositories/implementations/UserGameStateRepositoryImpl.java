package com.codedu.repositories.implementations;

import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.UserGameStateRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class UserGameStateRepositoryImpl extends GenericRepositoryImpl<UserGameState> implements UserGameStateRepository {

    public UserGameStateRepositoryImpl() {
        super(UserGameState.class);
    }

    @Override
    public Optional<UserGameState> findByUserId(int userId) {
        return getEntityManager()
                .createQuery(
                        "SELECT gs FROM UserGameState gs WHERE gs.user.id = :userId",
                        UserGameState.class
                )
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }
}

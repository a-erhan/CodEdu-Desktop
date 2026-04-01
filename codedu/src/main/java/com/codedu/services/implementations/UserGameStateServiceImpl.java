package com.codedu.services.implementations;

import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.UserGameStateRepository;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.UserGameStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserGameStateServiceImpl implements UserGameStateService {

    private final UserGameStateRepository userGameStateRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserGameStateServiceImpl(UserGameStateRepository userGameStateRepository, UserRepository userRepository) {
        this.userGameStateRepository = userGameStateRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void ensureGameStateForUser(User user) {
        if (user == null || user.getId() <= 0) {
            return;
        }
        Optional<UserGameState> existing = userGameStateRepository.findByUserId(user.getId());
        if (existing.isPresent()) {
            user.setGameState(existing.get());
            return;
        }
        UserGameState gs = UserGameState.newDefault();
        user.setGameState(gs);
        userRepository.update(user);
    }

    @Override
    @Transactional(readOnly = true)
    public void refreshGameStateOnUser(User user) {
        if (user == null || user.getId() <= 0) {
            return;
        }
        userGameStateRepository.findByUserId(user.getId()).ifPresent(user::setGameState);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserGameState> findByUserId(int userId) {
        return userGameStateRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void addXpAndTokens(int userId, int xpDelta, int tokenDelta) {
        UserGameState gs = userGameStateRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No game state for user id " + userId));
        gs.addXpAndResolveLevelUps(xpDelta);
        gs.setTokenBalance(gs.getTokenBalance() + tokenDelta);
        gs.setLastActivityDate(LocalDateTime.now());
        userGameStateRepository.update(gs);
    }

    @Override
    @Transactional
    public boolean tryDeductTokens(int userId, int amount) {
        if (amount <= 0) {
            return true;
        }
        Optional<UserGameState> opt = userGameStateRepository.findByUserId(userId);
        if (opt.isEmpty()) {
            return false;
        }
        UserGameState gs = opt.get();
        if (!gs.hasEnoughTokens(amount)) {
            return false;
        }
        gs.setTokenBalance(gs.getTokenBalance() - amount);
        gs.setLastActivityDate(LocalDateTime.now());
        userGameStateRepository.update(gs);
        return true;
    }
}

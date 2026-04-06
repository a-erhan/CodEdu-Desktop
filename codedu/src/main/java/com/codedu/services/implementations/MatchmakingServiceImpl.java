package com.codedu.services.implementations;

import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.services.interfaces.MatchmakingService;
import com.codedu.models.learning.Question;
import com.codedu.models.matchmaking.GameRoom;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Thread-safe matchmaking service.
 *
 * <p>
 * Uses a {@link ConcurrentLinkedQueue} for safe multi-threaded access.
 * The critical compound check-and-double-poll is wrapped in
 * {@code synchronized}
 * to prevent race conditions when multiple players join simultaneously.
 */
@Service
public class MatchmakingServiceImpl implements MatchmakingService {

    private final ConcurrentLinkedQueue<User> playerQueue = new ConcurrentLinkedQueue<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final QuestionRepository questionRepository;
    private final com.codedu.repositories.interfaces.UserRepository userRepository;

    @Autowired
    public MatchmakingServiceImpl(SimpMessagingTemplate messagingTemplate,
            QuestionRepository questionRepository, com.codedu.repositories.interfaces.UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Adds the given user to the waiting queue.
     * If a second player is already waiting, immediately pairs them and
     * broadcasts a {@link GameRoom} to both players' private STOMP channels.
     */
    public synchronized void joinQueue(User user) {
        boolean alreadyWaiting = playerQueue.stream().anyMatch(u -> u.getId() == user.getId());
        if (alreadyWaiting) {
            return;
        }

        playerQueue.offer(user);
        System.out.println("[Matchmaking] " + user.getUsername() + " joined queue. Queue size: " + playerQueue.size());

        if (playerQueue.size() >= 2) {
            User player1 = playerQueue.poll();
            User player2 = playerQueue.poll();
            if (player1 != null && player2 != null) {
                createAndDispatchGameRoom(player1, player2);
            }
        }
    }

    /**
     * Removes the user with the given id from the waiting queue (cancel
     * matchmaking).
     */
    public synchronized void leaveQueue(int userId) {
        playerQueue.removeIf(u -> u.getId() == userId);
        System.out.println("[Matchmaking] Player " + userId + " left the queue.");
    }

    /**
     * Broadcasts the match result to both players in the room, and applies rewards.
     */
    @Override
    @Transactional
    public void reportWin(com.codedu.models.matchmaking.MatchResult result) {
        if (result == null) return;
        
        System.out.println("[Matchmaking] Match over! Winner ID: " + result.getWinnerId());
        
        // Gamification
        try {
            userRepository.findById(result.getWinnerId()).ifPresent(winner -> {
                com.codedu.models.user.UserGameState state = winner.getGameState();
                if (state != null) {
                    state.setXp(state.getXp() + 50);
                    state.setTokenBalance(state.getTokenBalance() + 50);
                    userRepository.update(winner);
                }
            });
            userRepository.findById(result.getLoserId()).ifPresent(loser -> {
                com.codedu.models.user.UserGameState state = loser.getGameState();
                if (state != null) {
                    state.setXp(Math.max(0, state.getXp() - 50));
                    state.setTokenBalance(Math.max(0, state.getTokenBalance() - 50));
                    userRepository.update(loser);
                }
            });
        } catch (Exception e) {
            System.err.println("[Matchmaking] ERROR updating gamification: " + e.getMessage());
        }
        
        String dest1 = "/topic/match/" + result.getWinnerId();
        String dest2 = "/topic/match/" + result.getLoserId();
        
        try {
            messagingTemplate.convertAndSend(dest1, result);
            messagingTemplate.convertAndSend(dest2, result);
        } catch (Exception e) {
            System.err.println("[Matchmaking] ERROR broadcasting match result: " + e.getMessage());
        }
    }

    @Override
    public void broadcastAttempt(com.codedu.models.matchmaking.MatchAttemptUpdate update) {
        if (update == null) return;
        String dest = "/topic/match/" + update.getTargetId();
        try {
            messagingTemplate.convertAndSend(dest, update);
        } catch (Exception e) {
            System.err.println("[Matchmaking] ERROR broadcasting match attempt: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void createAndDispatchGameRoom(User player1, User player2) {
        String roomId = UUID.randomUUID().toString();
        CodeImplementationQuestion question = pickRandomCodeQuestion();

        GameRoom gameRoom = new GameRoom(roomId, player1, player2, question);

        // Send to each player's private channel
        String dest1 = "/topic/match/" + player1.getId();
        String dest2 = "/topic/match/" + player2.getId();
        System.out.println("[Matchmaking] Sending GameRoom to destinations: " + dest1 + " and " + dest2);
        try {
            messagingTemplate.convertAndSend(dest1, gameRoom);
            System.out.println("[Matchmaking] Successfully sent to " + dest1);
        } catch (Exception e) {
            System.err.println("[Matchmaking] ERROR sending to " + dest1 + ": " + e.getMessage());
            e.printStackTrace();
        }
        try {
            messagingTemplate.convertAndSend(dest2, gameRoom);
            System.out.println("[Matchmaking] Successfully sent to " + dest2);
        } catch (Exception e) {
            System.err.println("[Matchmaking] ERROR sending to " + dest2 + ": " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[Matchmaking] Match created — "
                + player1.getUsername() + " vs " + player2.getUsername()
                + " | Room: " + roomId);
    }

    private CodeImplementationQuestion pickRandomCodeQuestion() {
        List<Question> all = questionRepository.getAll();
        List<CodeImplementationQuestion> codeQuestions = all.stream()
                .filter(q -> q instanceof CodeImplementationQuestion)
                .map(q -> (CodeImplementationQuestion) q)
                .collect(Collectors.toList());

        if (codeQuestions.isEmpty()) {
            return null;
        }
        return codeQuestions.get(new Random().nextInt(codeQuestions.size()));
    }
}

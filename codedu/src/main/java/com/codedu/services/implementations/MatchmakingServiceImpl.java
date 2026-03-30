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

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Thread-safe matchmaking service.
 *
 * <p>Uses a {@link ConcurrentLinkedQueue} for safe multi-threaded access.
 * The critical compound check-and-double-poll is wrapped in {@code synchronized}
 * to prevent race conditions when multiple players join simultaneously.
 */
@Service
public class MatchmakingServiceImpl implements MatchmakingService {

    private final ConcurrentLinkedQueue<User> playerQueue = new ConcurrentLinkedQueue<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final QuestionRepository questionRepository;

    @Autowired
    public MatchmakingServiceImpl(SimpMessagingTemplate messagingTemplate,
                              QuestionRepository questionRepository) {
        this.messagingTemplate = messagingTemplate;
        this.questionRepository = questionRepository;
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
     * Removes the user with the given id from the waiting queue (cancel matchmaking).
     */
    public synchronized void leaveQueue(int userId) {
        playerQueue.removeIf(u -> u.getId() == userId);
        System.out.println("[Matchmaking] Player " + userId + " left the queue.");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void createAndDispatchGameRoom(User player1, User player2) {
        String roomId = UUID.randomUUID().toString();
        CodeImplementationQuestion question = pickRandomCodeQuestion();

        GameRoom gameRoom = new GameRoom(roomId, player1, player2, question);

        // Send to each player's private channel
        messagingTemplate.convertAndSend("/queue/match/" + player1.getId(), gameRoom);
        messagingTemplate.convertAndSend("/queue/match/" + player2.getId(), gameRoom);

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

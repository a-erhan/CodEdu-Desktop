package com.codedu.controllers;

import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Spring STOMP message handler for matchmaking events.
 * Listens on /app/match.join and /app/match.leave.
 * This is NOT a JavaFX controller — it has no @FXML bindings.
 */
@Controller
public class MatchmakingWsController {

    private final MatchmakingService matchmakingService;
    private final UserRepository userRepository;

    @Autowired
    public MatchmakingWsController(MatchmakingService matchmakingService, UserRepository userRepository) {
        this.matchmakingService = matchmakingService;
        this.userRepository = userRepository;
    }

    /**
     * Client sends their userId (int) to /app/match.join.
     * We look up the full User and add them to the matchmaking queue.
     */
    @MessageMapping("/match.join")
    public void handleJoin(@Payload Integer userId, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        userRepository.findById(userId).ifPresent(user -> matchmakingService.joinQueue(user, sessionId));
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        matchmakingService.handleDisconnect(sessionId);
    }

    /**
     * Client sends their userId to /app/match.leave to cancel matchmaking.
     */
    @MessageMapping("/match.leave")
    public void handleLeave(@Payload Integer userId) {
        matchmakingService.leaveQueue(userId);
    }

    /**
     * Winning client sends the MatchResult to /app/match.win.
     * We broadcast the result to both players so they know the match ended.
     */
    @MessageMapping("/match.win")
    public void handleWin(@Payload com.codedu.models.matchmaking.MatchResult result) {
        matchmakingService.reportWin(result);
    }

    /**
     * Client sends their attempt count to /app/match.attempt.
     */
    @MessageMapping("/match.attempt")
    public void handleAttempt(@Payload com.codedu.models.matchmaking.MatchAttemptUpdate update) {
        matchmakingService.broadcastAttempt(update);
    }
}

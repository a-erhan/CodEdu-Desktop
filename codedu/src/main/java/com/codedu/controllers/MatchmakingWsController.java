package com.codedu.controllers;

import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

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
    public void handleJoin(@Payload Integer userId) {
        userRepository.findById(userId).ifPresent(matchmakingService::joinQueue);
    }

    /**
     * Client sends their userId to /app/match.leave to cancel matchmaking.
     */
    @MessageMapping("/match.leave")
    public void handleLeave(@Payload Integer userId) {
        matchmakingService.leaveQueue(userId);
    }
}

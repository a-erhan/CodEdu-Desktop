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

@Controller
public class MatchmakingWsController {

    private final MatchmakingService matchmakingService;
    private final UserRepository userRepository;

    @Autowired
    public MatchmakingWsController(MatchmakingService matchmakingService, UserRepository userRepository) {
        this.matchmakingService = matchmakingService;
        this.userRepository = userRepository;
    }

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

    @MessageMapping("/match.leave")
    public void handleLeave(@Payload Integer userId) {
        matchmakingService.leaveQueue(userId);
    }

    @MessageMapping("/match.win")
    public void handleWin(@Payload com.codedu.models.matchmaking.MatchResult result) {
        matchmakingService.reportWin(result);
    }

    @MessageMapping("/match.attempt")
    public void handleAttempt(@Payload com.codedu.models.matchmaking.MatchAttemptUpdate update) {
        matchmakingService.broadcastAttempt(update);
    }
}

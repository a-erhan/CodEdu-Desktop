package com.codedu.services.interfaces;

import com.codedu.dtos.ChatMessageDTO;
import com.codedu.models.matchmaking.GameRoom;
import com.codedu.models.matchmaking.MatchResult;

import java.util.function.Consumer;

public interface WebSocketClientService {

    void connect(String currentUserId, Consumer<ChatMessageDTO> onMessage);

    void sendMessage(ChatMessageDTO message);

    void connectAndJoinMatchmaking(int userId, Consumer<GameRoom> onMatchFound,
            Consumer<MatchResult> onMatchResult, java.util.function.Consumer<com.codedu.models.matchmaking.MatchAttemptUpdate> onAttemptUpdate);

    void leaveMatchmaking(int userId);

    void sendMatchResult(MatchResult result);

    void sendAttemptUpdate(com.codedu.models.matchmaking.MatchAttemptUpdate update);
}

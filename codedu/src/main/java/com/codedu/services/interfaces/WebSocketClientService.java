package com.codedu.services.interfaces;

import com.codedu.dtos.ChatMessageDTO;
import com.codedu.models.matchmaking.GameRoom;

import java.util.function.Consumer;

public interface WebSocketClientService {

    void connect(String currentUserId, Consumer<ChatMessageDTO> onMessage);

    void sendMessage(ChatMessageDTO message);

    void connectAndJoinMatchmaking(int userId, Consumer<GameRoom> onMatchFound);

    void leaveMatchmaking(int userId);
}

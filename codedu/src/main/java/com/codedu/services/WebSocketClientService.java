package com.codedu.services;

import com.codedu.dtos.ChatMessageDTO;
import com.codedu.models.matchmaking.GameRoom;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.function.Consumer;

@Service
public class WebSocketClientService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** STOMP session used for chat messages. */
    private StompSession chatSession;

    /** Separate STOMP session dedicated to matchmaking. */
    private StompSession matchSession;

    private static final String WS_URL = "ws://localhost:8080/ws-chat";

    // -------------------------------------------------------------------------
    // Chat (existing – unchanged)
    // -------------------------------------------------------------------------

    /**
     * Connects to the chat WebSocket and subscribes to the user's private
     * chat channel {@code /queue/messages/{currentUserId}}.
     */
    public void connect(String currentUserId, Consumer<ChatMessageDTO> onMessageReceived) {
        WebSocketStompClient stompClient = buildStompClient();

        try {
            chatSession = stompClient.connectAsync(WS_URL, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    session.subscribe("/queue/messages/" + currentUserId, new StompSessionHandlerAdapter() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return ChatMessageDTO.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            onMessageReceived.accept((ChatMessageDTO) payload);
                        }
                    });
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[WS-Chat] Connection error!");
        }
    }

    /** Sends a chat message to {@code /app/chat.send}. */
    public void sendMessage(ChatMessageDTO message) {
        if (chatSession != null && chatSession.isConnected()) {
            chatSession.send("/app/chat.send", message);
        }
    }

    // -------------------------------------------------------------------------
    // Matchmaking (new)
    // -------------------------------------------------------------------------

    /**
     * Opens a dedicated STOMP connection for matchmaking and subscribes to
     * the user's private match channel {@code /queue/match/{userId}}.
     *
     * <p>This uses its own {@code matchSession} so that it never interferes
     * with the chat session.
     *
     * @param userId       the current user's database id
     * @param onMatchFound callback invoked on the STOMP receive thread when a
     *                     {@link GameRoom} arrives — always dispatch UI work to
     *                     {@code Platform.runLater()} on the caller's side.
     */
    public void connectForMatchmaking(int userId, Consumer<GameRoom> onMatchFound) {
        WebSocketStompClient stompClient = buildStompClient();

        try {
            matchSession = stompClient.connectAsync(WS_URL, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    session.subscribe("/queue/match/" + userId, new StompSessionHandlerAdapter() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return GameRoom.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            onMatchFound.accept((GameRoom) payload);
                        }
                    });
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[WS-Match] Connection error for userId=" + userId);
        }
    }

    /**
     * Sends the current user's id to {@code /app/match.join} so the server
     * can add them to the matchmaking queue.
     *
     * <p>Must be called <em>after</em> {@link #connectForMatchmaking} has
     * established the session.
     *
     * @param userId the current user's database id
     */
    public void sendJoinQueue(int userId) {
        if (matchSession != null && matchSession.isConnected()) {
            matchSession.send("/app/match.join", userId);
        } else {
            System.err.println("[WS-Match] Cannot send join — matchSession not connected.");
        }
    }

    /**
     * Sends a leave signal to {@code /app/match.leave} and disconnects the
     * matchmaking session.
     *
     * @param userId the current user's database id
     */
    public void leaveMatchmaking(int userId) {
        if (matchSession != null && matchSession.isConnected()) {
            matchSession.send("/app/match.leave", userId);
            matchSession.disconnect();
        }
        matchSession = null;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private WebSocketStompClient buildStompClient() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        return client;
    }
}

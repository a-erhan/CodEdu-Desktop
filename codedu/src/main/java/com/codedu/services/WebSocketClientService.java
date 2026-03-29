package com.codedu.services;

import com.codedu.dtos.ChatMessageDTO;
import com.codedu.models.matchmaking.GameRoom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Manages all STOMP/WebSocket connections for the JavaFX client.
 *
 * <p>Two independent sessions are maintained:
 * <ul>
 *   <li>{@code chatSession}  — private chat messages</li>
 *   <li>{@code matchSession} — matchmaking queue and game-room delivery</li>
 * </ul>
 *
 * <p>Both connections are <b>fully asynchronous</b> (no blocking {@code .get()}).
 * All work runs on the STOMP networking thread, keeping the JavaFX UI thread free.
 *
 * <p>The server URL is read from {@code app.websocket.server-url} in
 * {@code application.properties}. For multi-machine setups, set it to the IP of
 * the machine running the Spring Boot server, e.g.
 * {@code ws://192.168.1.100:8080/ws-chat}.
 */
@Service
public class WebSocketClientService {

    @Value("${app.websocket.server-url:ws://localhost:8080/ws-chat}")
    private String wsUrl;

    /** STOMP session for private chat messages. */
    private volatile StompSession chatSession;

    /** STOMP session dedicated to matchmaking. */
    private volatile StompSession matchSession;

    // =========================================================================
    // Chat
    // =========================================================================

    /**
     * Opens an async STOMP connection and subscribes to
     * {@code /queue/messages/{currentUserId}} for real-time chat delivery.
     *
     * <p>Safe to call on the JavaFX Application Thread — does NOT block.
     *
     * @param currentUserId the logged-in user's id (as a string)
     * @param onMessage     callback invoked on the STOMP receive thread when a
     *                      message arrives; wrap UI mutations in
     *                      {@code Platform.runLater()}
     */
    public void connect(String currentUserId, Consumer<ChatMessageDTO> onMessage) {
        // Disconnect any stale session first
        if (chatSession != null && chatSession.isConnected()) {
            chatSession.disconnect();
        }

        buildStompClient().connectAsync(wsUrl, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                chatSession = session;

                session.subscribe("/queue/messages/" + currentUserId,
                        new StompSessionHandlerAdapter() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return ChatMessageDTO.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                onMessage.accept((ChatMessageDTO) payload);
                            }
                        });
            }

            @Override
            public void handleTransportError(StompSession session, Throwable ex) {
                System.err.println("[WS-Chat] Transport error for user " + currentUserId
                        + ": " + ex.getMessage());
            }
        });
    }

    /** Sends a chat message to {@code /app/chat.send}. */
    public void sendMessage(ChatMessageDTO message) {
        if (chatSession != null && chatSession.isConnected()) {
            chatSession.send("/app/chat.send", message);
        } else {
            System.err.println("[WS-Chat] Cannot send — chatSession not connected.");
        }
    }

    // =========================================================================
    // Matchmaking
    // =========================================================================

    /**
     * Opens an async STOMP connection for matchmaking.
     *
     * <p>Inside {@code afterConnected}, the client:
     * <ol>
     *   <li>Subscribes to {@code /queue/match/{userId}} — the private channel
     *       where the server delivers a {@link GameRoom} when a match is found.</li>
     *   <li>Immediately sends a join request to {@code /app/match.join}.</li>
     * </ol>
     *
     * <p>Steps 1 and 2 happen in that exact order on the same TCP connection,
     * so there is <b>no race condition</b>: the subscription is always
     * established before the server can ever try to deliver a match.
     *
     * <p>Safe to call on the JavaFX Application Thread — does NOT block.
     *
     * @param userId      the current user's database id
     * @param onMatchFound callback invoked on the STOMP receive thread when a
     *                     {@link GameRoom} arrives; wrap UI mutations in
     *                     {@code Platform.runLater()}
     */
    public void connectAndJoinMatchmaking(int userId, Consumer<GameRoom> onMatchFound) {
        // Clean up any previous matchmaking session
        if (matchSession != null && matchSession.isConnected()) {
            matchSession.disconnect();
        }

        buildStompClient().connectAsync(wsUrl, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                matchSession = session;

                // Step 1 — subscribe FIRST
                session.subscribe("/queue/match/" + userId,
                        new StompSessionHandlerAdapter() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return GameRoom.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                onMatchFound.accept((GameRoom) payload);
                            }
                        });

                // Step 2 — join the queue only after subscription is in place.
                // TCP guarantees the SUBSCRIBE frame arrives at the server
                // before the SEND frame on this same connection.
                session.send("/app/match.join", userId);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable ex) {
                System.err.println("[WS-Match] Transport error for userId=" + userId
                        + ": " + ex.getMessage());
            }
        });
    }

    /**
     * Cancels matchmaking: sends a leave signal and disconnects the session.
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

    // =========================================================================
    // Helpers
    // =========================================================================

    private WebSocketStompClient buildStompClient() {
        WebSocketStompClient client =
                new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        return client;
    }
}

package com.codedu.services.implementations;

import com.codedu.dtos.ChatMessageDTO;
import com.codedu.services.interfaces.WebSocketClientService;
import com.codedu.models.matchmaking.GameRoom;
import com.codedu.models.matchmaking.MatchResult;
import com.codedu.models.matchmaking.MatchAttemptUpdate;
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
 * <p>
 * Two independent sessions are maintained:
 * <ul>
 * <li>{@code chatSession} — private chat messages</li>
 * <li>{@code matchSession} — matchmaking queue and game-room delivery</li>
 * </ul>
 *
 * <p>
 * Both connections are <b>fully asynchronous</b> (no blocking {@code .get()}).
 * All work runs on the STOMP networking thread, keeping the JavaFX UI thread
 * free.
 *
 * <p>
 * The server URL is read from {@code app.websocket.server-url} in
 * {@code application.properties}. For multi-machine setups, set it to the IP of
 * the machine running the Spring Boot server, e.g.
 * {@code ws://192.168.1.100:8080/ws-chat}.
 */
@Service
public class WebSocketClientServiceImpl implements WebSocketClientService {

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
     * <p>
     * Safe to call on the JavaFX Application Thread — does NOT block.
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
                                try {
                                    onMessage.accept((ChatMessageDTO) payload);
                                } catch (Exception e) {
                                    System.err.println("[WS-Chat] Error in handleFrame: " + e.getMessage());
                                    e.printStackTrace();
                                }
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
     * <p>
     * Inside {@code afterConnected}, the client:
     * <ol>
     * <li>Subscribes to {@code /queue/match/{userId}} — the private channel
     * where the server delivers a {@link GameRoom} when a match is found.</li>
     * <li>Immediately sends a join request to {@code /app/match.join}.</li>
     * </ol>
     *
     * <p>
     * Steps 1 and 2 happen in that exact order on the same TCP connection,
     * so there is <b>no race condition</b>: the subscription is always
     * established before the server can ever try to deliver a match.
     *
     * <p>
     * Safe to call on the JavaFX Application Thread — does NOT block.
     *
     * @param userId       the current user's database id
     * @param onMatchFound callback invoked on the STOMP receive thread when a
     *                     {@link GameRoom} arrives; wrap UI mutations in
     *                     {@code Platform.runLater()}
     */
    public void connectAndJoinMatchmaking(int userId, Consumer<GameRoom> onMatchFound,
            Consumer<MatchResult> onMatchResult, Consumer<MatchAttemptUpdate> onAttemptUpdate) {
        // Clean up any previous matchmaking session
        if (matchSession != null && matchSession.isConnected()) {
            matchSession.disconnect();
        }

        buildStompClient().connectAsync(wsUrl, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                matchSession = session;
                String destination = "/topic/match/" + userId;
                System.out.println("[WS-Match] Connected. Session ID: " + session.getSessionId()
                        + ". Subscribing to: " + destination);

                // Step 1 — subscribe FIRST
                session.subscribe(destination,
                        new StompSessionHandlerAdapter() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                // Use byte[] so the STOMP converter delivers raw
                                // bytes without any Jackson deserialization — avoids
                                // silent failures caused by type mismatches.
                                return byte[].class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                System.out.println("[WS-Match] >>> handleFrame INVOKED!");
                                System.out.println("[WS-Match] RAW PAYLOAD: " + payload);
                                try {
                                    String json = payload instanceof byte[]
                                            ? new String((byte[]) payload, java.nio.charset.StandardCharsets.UTF_8)
                                            : payload.toString();

                                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                                    mapper.configure(
                                            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                            false);

                                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(json);
                                    System.out.println("[WS-Match] JSON keys: " + node.fieldNames());

                                    if (node.has("roomId") && node.has("player1")) {
                                        System.out.println("[WS-Match] Identified as GameRoom.");
                                        GameRoom room = mapper.treeToValue(node, GameRoom.class);
                                        System.out.println("[WS-Match] GameRoom parsed OK. roomId="
                                                + room.getRoomId());
                                        onMatchFound.accept(room);
                                        System.out.println("[WS-Match] onMatchFound callback completed.");
                                    } else if (node.has("winnerId")) {
                                        System.out.println("[WS-Match] Identified as MatchResult.");
                                        com.codedu.models.matchmaking.MatchResult result = mapper.treeToValue(node,
                                                com.codedu.models.matchmaking.MatchResult.class);
                                        if (onMatchResult != null) onMatchResult.accept(result);
                                        System.out.println("[WS-Match] onMatchResult callback completed.");
                                    } else if (node.has("attempts")) {
                                        System.out.println("[WS-Match] Identified as MatchAttemptUpdate.");
                                        com.codedu.models.matchmaking.MatchAttemptUpdate update = mapper.treeToValue(node,
                                                com.codedu.models.matchmaking.MatchAttemptUpdate.class);
                                        if (onAttemptUpdate != null) onAttemptUpdate.accept(update);
                                        System.out.println("[WS-Match] onAttemptUpdate callback completed.");
                                    } else {
                                        System.err.println("[WS-Match] Unknown message type: "
                                                + json.substring(0, Math.min(200, json.length())));
                                    }
                                } catch (Exception e) {
                                    System.err.println("[WS-Match] ERROR in handleFrame: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });

                System.out.println("[WS-Match] Subscribed. Now sending /app/match.join for userId=" + userId);
                // Step 2 — join the queue only after subscription is in place.
                session.send("/app/match.join", userId);
                System.out.println("[WS-Match] Join message sent for userId=" + userId);
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

    /**
     * Sends the match result (win) to the server.
     */
    public void sendMatchResult(MatchResult result) {
        if (matchSession != null && matchSession.isConnected()) {
            matchSession.send("/app/match.win", result);
            System.out.println(
                    "[WS-Match] Sent match.win for roomId=" + result.getRoomId() + ", winner=" + result.getWinnerId());
        } else {
            System.err.println("[WS-Match] Error: Cannot send MatchResult, session disconnected.");
        }
    }

    public void sendAttemptUpdate(MatchAttemptUpdate update) {
        if (matchSession != null && matchSession.isConnected()) {
            matchSession.send("/app/match.attempt", update);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private WebSocketStompClient buildStompClient() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        org.springframework.messaging.converter.ByteArrayMessageConverter byteConverter =
                new org.springframework.messaging.converter.ByteArrayMessageConverter();
        MappingJackson2MessageConverter jacksonConverter = new MappingJackson2MessageConverter();
        org.springframework.messaging.converter.CompositeMessageConverter composite =
                new org.springframework.messaging.converter.CompositeMessageConverter(
                        java.util.Arrays.asList(byteConverter, jacksonConverter));
        client.setMessageConverter(composite);
        return client;
    }
}

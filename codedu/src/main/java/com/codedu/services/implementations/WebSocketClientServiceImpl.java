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

@Service
public class WebSocketClientServiceImpl implements WebSocketClientService {

    @Value("${app.websocket.server-url:ws://localhost:8080/ws-chat}")
    private String wsUrl;

    private volatile StompSession chatSession;

    private volatile StompSession matchSession;

    public void connect(String currentUserId, Consumer<ChatMessageDTO> onMessage) {

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

    public void sendMessage(ChatMessageDTO message) {
        if (chatSession != null && chatSession.isConnected()) {
            chatSession.send("/app/chat.send", message);
        } else {
            System.err.println("[WS-Chat] Cannot send — chatSession not connected.");
        }
    }

    public void connectAndJoinMatchmaking(int userId, Consumer<GameRoom> onMatchFound,
            Consumer<MatchResult> onMatchResult, Consumer<MatchAttemptUpdate> onAttemptUpdate) {

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

                session.subscribe(destination,
                        new StompSessionHandlerAdapter() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {

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

    public void leaveMatchmaking(int userId) {
        if (matchSession != null && matchSession.isConnected()) {
            matchSession.send("/app/match.leave", userId);
            matchSession.disconnect();
        }
        matchSession = null;
    }

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

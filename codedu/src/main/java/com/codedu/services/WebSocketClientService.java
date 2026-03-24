package com.codedu.services;


import com.codedu.dtos.ChatMessageDTO;
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

    private StompSession stompSession;
    private final String WS_URL = "ws://localhost:8080/ws-chat";

    public void connect(String currentUserId, Consumer<ChatMessageDTO> onMessageReceived) {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            stompSession = stompClient.connectAsync(WS_URL, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

                    session.subscribe("/queue/messages/" + currentUserId, new StompSessionHandlerAdapter() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return ChatMessageDTO.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            ChatMessageDTO message = (ChatMessageDTO) payload;
                            onMessageReceived.accept(message);
                        }
                    });
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("WebSocket connection error!");
        }
    }

    public void sendMessage(ChatMessageDTO message) {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.send("/app/chat.send", message);
        }
    }
}
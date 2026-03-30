package com.codedu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Force the inbound channel to a single-threaded executor.
     *
     * <p>Spring's default {@code clientInboundChannel} uses a thread pool sized at
     * {@code availableProcessors * 2}. The pool dispatches each incoming frame to all
     * channel subscribers (SimpleBrokerMessageHandler, SimpAnnotationMethodMessageHandler)
     * as independent tasks, so they run in parallel.
     *
     * <p>Consequence for matchmaking: Player 2's SUBSCRIBE and JOIN frames arrive in
     * TCP order, but Task(SUBSCRIBE registration) and Task(JOIN → joinQueue → broadcast)
     * are submitted to the pool and can execute concurrently. If the JOIN task runs
     * first, {@code createAndDispatchGameRoom} calls
     * {@code convertAndSend("/queue/match/{p2Id}", gameRoom)} before P2's subscription
     * is registered in {@code DefaultSubscriptionRegistry}. The broker finds no
     * subscriber for that destination and silently drops the message.
     *
     * <p>With {@code corePoolSize(1)} every frame is processed strictly in FIFO order:
     * SUBSCRIBE registers → JOIN processes → broadcast finds the subscription. No race.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(1)
                .maxPoolSize(1);
    }
}
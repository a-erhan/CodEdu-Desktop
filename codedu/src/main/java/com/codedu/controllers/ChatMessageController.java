package com.codedu.controllers;

import com.codedu.dtos.ChatMessageDTO;
import com.codedu.models.social.ChatMessage;
import com.codedu.repositories.interfaces.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageController(SimpMessagingTemplate messagingTemplate,
            ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        // Persist message to database
        try {
            ChatMessage entity = ChatMessage.builder()
                    .senderId(Integer.parseInt(chatMessageDTO.getSenderId()))
                    .receiverId(Integer.parseInt(chatMessageDTO.getReceiverId()))
                    .content(chatMessageDTO.getContent())
                    .timestampMillis(chatMessageDTO.getTimestamp())
                    .build();
            chatMessageRepository.save(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Forward via WebSocket for real-time delivery
        messagingTemplate.convertAndSend("/queue/messages/" + chatMessageDTO.getReceiverId(), chatMessageDTO);
    }
}

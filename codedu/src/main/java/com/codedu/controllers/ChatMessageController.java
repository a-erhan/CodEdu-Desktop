package com.codedu.controllers;


import com.codedu.dtos.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        messagingTemplate.convertAndSend("/queue/messages/" + chatMessageDTO.getReceiverId(), chatMessageDTO);
    }
}

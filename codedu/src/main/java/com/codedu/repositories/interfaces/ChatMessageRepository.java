package com.codedu.repositories.interfaces;

import com.codedu.models.social.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends GenericRepository<ChatMessage> {
    List<ChatMessage> findConversation(int userId1, int userId2);
}

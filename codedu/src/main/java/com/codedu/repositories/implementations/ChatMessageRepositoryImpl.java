package com.codedu.repositories.implementations;

import com.codedu.models.social.ChatMessage;
import com.codedu.repositories.interfaces.ChatMessageRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository
@Transactional
public class ChatMessageRepositoryImpl extends GenericRepositoryImpl<ChatMessage> implements ChatMessageRepository {

    public ChatMessageRepositoryImpl() {
        super(ChatMessage.class);
    }

    @Override
    public List<ChatMessage> findConversation(int userId1, int userId2) {
        return getEntityManager().createQuery(
                "SELECT m FROM ChatMessage m " +
                        "WHERE m.isDeleted = false " +
                        "AND ((m.senderId = :u1 AND m.receiverId = :u2) " +
                        "  OR (m.senderId = :u2 AND m.receiverId = :u1)) " +
                        "ORDER BY m.timestampMillis ASC",
                ChatMessage.class)
                .setParameter("u1", userId1)
                .setParameter("u2", userId2)
                .getResultList();
    }
}

package com.codedu.models.social;

import com.codedu.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Column(name = "sender_id", nullable = false)
    private int senderId;

    @Column(name = "receiver_id", nullable = false)
    private int receiverId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp_millis", nullable = false)
    private long timestampMillis;
}

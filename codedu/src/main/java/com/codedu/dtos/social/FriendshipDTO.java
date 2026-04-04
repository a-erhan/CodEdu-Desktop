package com.codedu.dtos.social;

import com.codedu.models.social.Friendship;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record FriendshipDTO(
    int id,
    int requesterId,
    String requesterUsername,
    int receiverId,
    String receiverUsername,
    Friendship.FriendshipStatus status,
    LocalDateTime createdAt
) {}

package com.codedu.dtos.social;

import com.codedu.models.social.Friendship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipDTO {
    private int id;
    private int requesterId;
    private String requesterUsername;
    private int receiverId;
    private String receiverUsername;
    private Friendship.FriendshipStatus status;
    private LocalDateTime createdAt;
}

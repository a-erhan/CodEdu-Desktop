package com.codedu.models.user;

import lombok.*;
import jakarta.persistence.*;
import com.codedu.models.BaseEntity;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.PENDING;

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        BLOCKED
    }
}

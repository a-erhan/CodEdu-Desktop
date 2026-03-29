package com.codedu.models.matchmaking;

import com.codedu.models.BaseEntity;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Transient DTO representing an active 1v1 match room.
 * Not a JPA entity — created in-memory by MatchmakingService and
 * broadcast to both players via STOMP.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom extends BaseEntity {

    private String roomId;
    private User player1;
    private User player2;
    private CodeImplementationQuestion question;
}

package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import com.codedu.models.matchmaking.Competitor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Prevent Jackson from traversing Hibernate lazy-proxy fields during STOMP
// serialisation.
// 'hibernateLazyInitializer' and 'handler' are Hibernate proxy internals.
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "competitor", "gameState", "inventory" })
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.STUDENT;

    @Builder.Default
    private boolean isActive = true;

    @Column(name = "token_balance")
    @Builder.Default
    private int tokenBalance = 0;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor_id")
    private Competitor competitor;

    /**
     * Inverse side: FK {@code user_id} is stored on {@link UserGameState}
     * ({@code user_game_states.user_id}).
     * Use {@link #setGameState(UserGameState)} so the owning side stays in sync.
     */
    @Setter(AccessLevel.NONE)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private UserGameState gameState;

    public void setGameState(UserGameState gameState) {
        if (this.gameState == gameState) {
            return;
        }
        if (this.gameState != null) {
            this.gameState.internalSetUser(null);
        }
        this.gameState = gameState;
        if (gameState != null) {
            gameState.internalSetUser(this);
        }
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private UserInventory inventory;

    public boolean login(String email, String password) {
        if (email == null || password == null) {
            return false;
        }
        return this.email != null
                && this.email.equalsIgnoreCase(email.trim())
                && this.password.equals(password)
                && isActive;
    }
}
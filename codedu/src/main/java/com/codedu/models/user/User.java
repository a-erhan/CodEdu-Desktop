package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import com.codedu.models.matchmaking.Competitor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Prevent Jackson from traversing Hibernate lazy-proxy fields during STOMP serialisation.
// 'hibernateLazyInitializer' and 'handler' are Hibernate proxy internals.
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "competitor", "gameState", "inventory"})
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_state_id")
    private UserGameState gameState;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private UserInventory inventory;

    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password) && isActive;
    }
}
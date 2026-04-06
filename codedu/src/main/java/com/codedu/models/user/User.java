package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.social.ForumPost;
import com.codedu.models.social.Friendship;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "competitor", "gameState", "inventory", "sentFriendRequests", "receivedFriendRequests", "posts" })
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor_id")
    private Competitor competitor;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "game_state_id")
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

    @Builder.Default
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Friendship> sentFriendRequests = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Friendship> receivedFriendRequests = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumPost> posts = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserInventory inventory;

    public void setInventory(UserInventory inventory) {
        if (this.inventory != null) {
            this.inventory.setUser(null);
        }
        if (inventory != null) {
            inventory.setUser(this);
        }
        this.inventory = inventory;
    }



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
package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import com.codedu.models.matchmaking.Competitor;
import com.codedu.models.social.ForumPost;
import com.codedu.models.social.Friendship;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
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
@ToString(exclude = {"competitor", "gameState", "inventory", "sentFriendRequests", "receivedFriendRequests", "posts"})
@EqualsAndHashCode(callSuper = true, exclude = {"competitor", "gameState", "inventory", "sentFriendRequests", "receivedFriendRequests", "posts"})
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

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 64)
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private Instant emailVerificationExpiresAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Competitor competitor;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserGameState gameState;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserInventory inventory;

    @Builder.Default
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Friendship> sentFriendRequests = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Friendship> receivedFriendRequests = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumPost> posts = new ArrayList<>();

    public void setCompetitor(Competitor competitor) {
        if (competitor == null) {
            if (this.competitor != null) this.competitor.setUser(null);
        } else {
            competitor.setUser(this);
        }
        this.competitor = competitor;
    }

    public void setGameState(UserGameState gameState) {
        if (gameState == null) {
            if (this.gameState != null) this.gameState.internalSetUser(null);
        } else {
            gameState.internalSetUser(this);
        }
        this.gameState = gameState;
    }

    public void setInventory(UserInventory inventory) {
        if (inventory == null) {
            if (this.inventory != null) this.inventory.setUser(null);
        } else {
            inventory.setUser(this);
        }
        this.inventory = inventory;
    }

    public boolean login(String email, String password) {
        if (email == null || password == null) return false;
        return this.email != null
                && this.email.equalsIgnoreCase(email.trim())
                && this.password.equals(password)
                && isActive;
    }

    public String getDisplayName() {
        if (email != null && !email.isBlank()) {
            int at = email.indexOf('@');
            if (at > 0) return email.substring(0, at);
        }
        if (username != null && !username.isBlank()) return username;
        return "User";
    }

    public boolean isTesterAccount() {
        return "tester".equalsIgnoreCase(username) || "tester".equalsIgnoreCase(getDisplayName());
    }
}
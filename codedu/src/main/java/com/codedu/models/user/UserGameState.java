package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import com.codedu.models.gamification.Achievement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_game_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGameState extends BaseEntity {

    /**
     * Default progression row for a new account (matches shell bootstrap and seeder expectations).
     */
    public static UserGameState newDefault() {
        return UserGameState.builder()
                .level(1)
                .xp(0)
                .heartCount(3)
                .tokenBalance(0)
                .currentStreak(0)
                .build();
    }

    /**
     * Owning side of the one-to-one: {@code user_game_states.user_id} references {@code users.id}.
     * {@link User#setGameState(UserGameState)} keeps this reference and {@link User#gameState} aligned.
     */
    @Setter(AccessLevel.NONE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = true)
    private User user;

    /**
     * Maintains bidirectional consistency with {@link User#setGameState(UserGameState)}; do not set {@link #user} directly.
     */
    void internalSetUser(User user) {
        this.user = user;
    }

    private int heartCount;
    private int level;
    private int xp;
    private int tokenBalance;
    private int currentStreak;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_achievements",
            joinColumns = @JoinColumn(name = "user_game_state_id"),
            inverseJoinColumns = @JoinColumn(name = "achievement_id")
    )
    @Builder.Default
    private List<Achievement> achievements = new ArrayList<>();

    public int getXpToNextLevel() {
        return (this.level * 1000) - this.xp;
    }

    public void addXpAndResolveLevelUps(int xpDelta) {
        if (xpDelta <= 0) {
            return;
        }
        this.xp += xpDelta;
        while (this.xp >= (this.level * 1000)) {
            this.xp -= (this.level * 1000);
            this.level += 1;
        }
    }

    public boolean hasEnoughTokens(int amount) {
        return this.tokenBalance >= amount;
    }

    public void addHeart() {
        if (this.heartCount < 5) {
            this.heartCount++;
        }
    }

    @UpdateTimestamp
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;
}
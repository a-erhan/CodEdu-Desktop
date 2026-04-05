package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import com.codedu.models.gamification.Achievement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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

    public static final int MAX_HEARTS = 15;

    /**
     * Default progression row for a new account (matches shell bootstrap and seeder
     * expectations).
     */
    public static UserGameState newDefault() {
        return UserGameState.builder()
                .level(1)
                .xp(0)
                .heartCount(3)
                .tokenBalance(100)
                .currentStreak(0)
                .build();
    }

    @Setter(AccessLevel.NONE)
    @OneToOne(mappedBy = "gameState", fetch = FetchType.LAZY)
    private User user;

    /**
     * Maintains bidirectional consistency with
     * {@link User#setGameState(UserGameState)}; do not set {@link #user} directly.
     */
    void internalSetUser(User user) {
        this.user = user;
    }

    private int heartCount;
    private int level;
    private int xp;
    private int tokenBalance;

    @Column(name = "current_streak")
    private int currentStreak = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_achievements", joinColumns = @JoinColumn(name = "user_game_state_id"), inverseJoinColumns = @JoinColumn(name = "achievement_id"))
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
        if (this.heartCount < MAX_HEARTS) {
            this.heartCount++;
        }
    }

    @UpdateTimestamp
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;
}
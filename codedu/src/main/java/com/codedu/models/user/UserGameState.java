package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import com.codedu.models.gamification.Achievement;
import jakarta.persistence.*;
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
@ToString(exclude = {"user", "achievements"})
@EqualsAndHashCode(callSuper = true, exclude = {"user", "achievements"})
public class UserGameState extends BaseEntity {

    public static final int MAX_HEARTS = 15;

    public static UserGameState newDefault() {
        return UserGameState.builder()
                .level(1)
                .xp(0)
                .heartCount(15)
                .tokenBalance(100)
                .currentStreak(0)
                .build();
    }

    @Setter(AccessLevel.NONE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    void internalSetUser(User user) {
        this.user = user;
    }

    private int heartCount;
    private int level;
    private int xp;
    private int tokenBalance;

    @Builder.Default
    @Column(name = "current_streak")
    private int currentStreak = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(name = "double_xp_active_until")
    private LocalDateTime doubleXpActiveUntil;

    @UpdateTimestamp
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

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
        if (xpDelta <= 0) return;
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
        if (this.heartCount < MAX_HEARTS) this.heartCount++;
    }

    public boolean isDoubleXpActive() {
        return doubleXpActiveUntil != null && LocalDateTime.now().isBefore(doubleXpActiveUntil);
    }

    public void extendDoubleXpMinutes(int minutes) {
        if (minutes <= 0) return;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = doubleXpActiveUntil != null && now.isBefore(doubleXpActiveUntil)
                ? doubleXpActiveUntil
                : now;
        this.doubleXpActiveUntil = base.plusMinutes(minutes);
    }

    public int withDoubleXpApplied(int baseXp) {
        if (baseXp <= 0) return baseXp;
        return isDoubleXpActive() ? baseXp * 2 : baseXp;
    }
}
package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int heartCount;
    private int level;
    private int xp;

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

    public void addHeart() {
        if (this.heartCount < 5) {
            this.heartCount++;
        }
    }
}
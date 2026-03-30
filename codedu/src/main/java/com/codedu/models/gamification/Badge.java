package com.codedu.models.gamification;

import com.codedu.models.BaseEntity;
import com.codedu.models.user.UserGameState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Badge extends BaseEntity {
    private String title;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    private String description;
    private String iconURL;

    public boolean isEarned(UserGameState gameState) {
        return gameState.getAchievements().contains(this.achievement);
    }

    public String getDisplayName() {
        return title.toUpperCase();
    }
}

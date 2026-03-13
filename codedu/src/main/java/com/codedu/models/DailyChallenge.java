package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyChallenge extends BaseEntity {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private Reward reward;

    public int calculateTotalXp(int userMultiplier) {
        return this.reward.getXp() * userMultiplier;
    }
}
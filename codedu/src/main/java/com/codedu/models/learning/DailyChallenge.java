package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(name = "daily_challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyChallenge extends BaseEntity {

    private String name;

    private LocalDate targetDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "daily_challenge_questions",
            joinColumns = @JoinColumn(name = "daily_challenge_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @Builder.Default
    private List<Question> questions = new ArrayList<>();
    @Embedded
    private Reward reward;

    public int calculateTotalXp(int userMultiplier) {
        return this.reward.getXp() * userMultiplier;
    }
}
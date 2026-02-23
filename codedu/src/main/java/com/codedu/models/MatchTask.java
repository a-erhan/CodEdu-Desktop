package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchTask extends BaseEntity {

    @ElementCollection
    @CollectionTable(name = "match_task_questions", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "question_id")
    @Builder.Default
    private List<Long> questionIds = new ArrayList<>();

    private int xpReward;
    private int tokenReward;

    public void addQuestionId(Long questionId) {
        if (!this.questionIds.contains(questionId)) {
            this.questionIds.add(questionId);
        }
    }
}
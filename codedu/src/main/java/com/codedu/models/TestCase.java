package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase extends BaseEntity {

    private String input;

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    private boolean isHidden;

    private float cpuTimeLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private CodeImplementationQuestion ques;

    public boolean validate(String userOutput) {
        if (userOutput == null || expectedOutput == null) {
            return false;
        }

        return expectedOutput.trim().equalsIgnoreCase(userOutput.trim());
    }
}
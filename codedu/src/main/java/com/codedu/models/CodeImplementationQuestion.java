package com.codedu.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeImplementationQuestion extends Question {
    //Required for Judge0 API
    @Column(columnDefinition = "TEXT")
    private String boilerplateCode;

    @OneToMany(mappedBy = "ques", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestCase> testCases = new ArrayList<>();

    @Override
    public boolean validateAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        if (testCases == null || testCases.isEmpty()) {
            return false;
        }

        for (TestCase testCase : testCases) {
            if (testCase == null || !testCase.validate(userAnswer)) {
                return false;
            }
        }

        return true;
    }
}
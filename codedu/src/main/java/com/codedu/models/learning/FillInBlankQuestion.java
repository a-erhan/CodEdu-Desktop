package com.codedu.models.learning;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class FillInBlankQuestion extends Question {

    @Override
    public boolean validateAnswer(String userAnswer) {

        if (userAnswer == null || getSolution() == null) {
            return false;
        }

        return getSolution()
                .trim()
                .equalsIgnoreCase(userAnswer.trim());
    }
}
package com.codedu.models;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class MultipleChoiceQuestion extends Question{
    private List<String> choices;

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

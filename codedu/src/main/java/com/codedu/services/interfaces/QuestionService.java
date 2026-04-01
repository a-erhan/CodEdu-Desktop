package com.codedu.services.interfaces;

import com.codedu.models.learning.Question;

import java.util.Optional;

public interface QuestionService {

    Optional<Question> getQuestionById(int id);

    void saveQuestion(Question question);
}

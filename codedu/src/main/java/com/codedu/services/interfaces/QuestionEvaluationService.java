package com.codedu.services.interfaces;

import com.codedu.dtos.learning.QuestionDTO;
import com.codedu.models.learning.Question;

public interface QuestionEvaluationService {
    // Used by Learning Path / QuestionSolverController
    boolean evaluate(QuestionDTO questionDto, String userAnswer);


    boolean evaluate(Question question, String userAnswer);
}
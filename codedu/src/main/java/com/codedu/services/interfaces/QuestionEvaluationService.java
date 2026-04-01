package com.codedu.services.interfaces;

import com.codedu.models.learning.Question;

public interface QuestionEvaluationService {

    boolean evaluate(Question question, String userAnswer);
}

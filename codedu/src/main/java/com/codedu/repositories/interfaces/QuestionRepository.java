package com.codedu.repositories.interfaces;

import com.codedu.models.learning.Question;

import com.codedu.models.learning.QuestionDifficulity;
import com.codedu.models.learning.QuestionType;
import java.util.List;

public interface QuestionRepository extends GenericRepository<Question> {
    List<Question> findByQuestionType(QuestionType type);
    List<Question> findByQuestionDifficulity(QuestionDifficulity difficulity);
    List<Question> getRandomQuestions(int count);
}

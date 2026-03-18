package com.codedu.repositories.interfaces;

import com.codedu.models.Question;

import com.codedu.models.QuestionDifficulity;
import com.codedu.models.QuestionType;
import java.util.List;

public interface QuestionRepository extends GenericRepository<Question> {
    List<Question> findByQuestionType(QuestionType type);
    List<Question> findByQuestionDifficulity(QuestionDifficulity difficulity);
    List<Question> getRandomQuestions(int count);
}

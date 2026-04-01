package com.codedu.repositories.interfaces;

import com.codedu.models.learning.Question;

import com.codedu.models.learning.QuestionDifficulty;
import com.codedu.models.learning.QuestionType;
import java.util.List;

public interface QuestionRepository extends GenericRepository<Question> {
    List<Question> findByQuestionType(QuestionType type);
    List<Question> findByQuestionDifficulity(QuestionDifficulty difficulity);
    List<Question> getRandomQuestions(int count);
}

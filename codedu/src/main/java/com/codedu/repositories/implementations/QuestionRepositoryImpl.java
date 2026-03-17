package com.codedu.repositories.implementations;

import com.codedu.models.Question;
import com.codedu.repositories.interfaces.QuestionRepository;
import com.codedu.models.QuestionDifficulity;
import com.codedu.models.QuestionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository
@Transactional
public class QuestionRepositoryImpl extends GenericRepositoryImpl<Question> implements QuestionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public QuestionRepositoryImpl() {
        super(Question.class);
    }

    @Override
    public List<Question> findByQuestionType(QuestionType type) {
        return entityManager.createQuery(
                "SELECT q FROM Question q WHERE q.questionType = :type AND q.isDeleted = false", 
                Question.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public List<Question> findByQuestionDifficulity(QuestionDifficulity difficulity) {
        return entityManager.createQuery(
                "SELECT q FROM Question q WHERE q.questionDifficulity = :difficulity AND q.isDeleted = false", 
                Question.class)
                .setParameter("difficulity", difficulity)
                .getResultList();
    }
}

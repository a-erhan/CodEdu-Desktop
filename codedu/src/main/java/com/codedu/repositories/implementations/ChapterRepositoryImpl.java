package com.codedu.repositories.implementations;

import com.codedu.models.learning.Chapter;
import com.codedu.repositories.interfaces.ChapterRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository
@Transactional
public class ChapterRepositoryImpl extends GenericRepositoryImpl<Chapter> implements ChapterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ChapterRepositoryImpl() {
        super(Chapter.class);
    }

    @Override
    public List<Chapter> findByDifficulty(Chapter.Difficulty difficulty) {
        return entityManager.createQuery(
                "SELECT c FROM Chapter c WHERE c.difficulty = :difficulty AND c.isDeleted = false", 
                Chapter.class)
                .setParameter("difficulty", difficulty)
                .getResultList();
    }

    @Override
    public List<Chapter> findAll() {
        return entityManager.createQuery("SELECT c FROM Chapter c", Chapter.class).getResultList();
    }

    @Override
    public List<Chapter> findByTopicName(String topicName) {
        return entityManager.createQuery(
                "SELECT c FROM Chapter c WHERE c.topicName = :topicName AND c.isDeleted = false", 
                Chapter.class)
                .setParameter("topicName", topicName)
                .getResultList();
    }
}

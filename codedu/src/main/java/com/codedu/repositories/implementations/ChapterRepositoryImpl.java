package com.codedu.repositories.implementations;

import com.codedu.models.learning.Chapter;
import com.codedu.repositories.interfaces.ChapterRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException; // 🚀 Add this import
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional; // 🚀 Add this import

@Repository
@Transactional
public class ChapterRepositoryImpl extends GenericRepositoryImpl<Chapter> implements ChapterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ChapterRepositoryImpl() {
        super(Chapter.class);
    }

    // 🚀 THE MISSING METHOD: Implementing the Fetch Join manually
    @Override
    public Optional<Chapter> findByIdWithQuestions(Long id) {
        try {
            Chapter chapter = entityManager.createQuery(
                            "SELECT c FROM Chapter c " +
                                    "LEFT JOIN FETCH c.content cc " +
                                    "LEFT JOIN FETCH cc.questions " +
                                    "WHERE c.id = :id", Chapter.class)
                    .setParameter("id", id)
                    .getSingleResult();

            return Optional.ofNullable(chapter);
        } catch (NoResultException e) {
            return Optional.empty();
        }
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
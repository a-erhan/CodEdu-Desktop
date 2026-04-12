package com.codedu.repositories.implementations;

import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.UserChapterProgress;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.UserChapterProgressRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserChapterProgressRepositoryImpl extends GenericRepositoryImpl<UserChapterProgress> implements UserChapterProgressRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public UserChapterProgressRepositoryImpl() {
        super(UserChapterProgress.class);
    }

    @Override
    public Optional<UserChapterProgress> findByUserAndChapter(User user, Chapter chapter) {
        try {
            UserChapterProgress progress = entityManager
                    .createQuery("SELECT p FROM UserChapterProgress p WHERE p.user = :user AND p.chapter = :chapter AND p.isDeleted = false", UserChapterProgress.class)
                    .setParameter("user", user)
                    .setParameter("chapter", chapter)
                    .getSingleResult();
            return Optional.of(progress);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<UserChapterProgress> findByUser(User user) {
        return entityManager
                .createQuery("SELECT p FROM UserChapterProgress p WHERE p.user = :user AND p.isDeleted = false", UserChapterProgress.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public Optional<UserChapterProgress> findByUserIdAndChapterIdDetailed(Long userId, Long chapterId) {
        try {

            UserChapterProgress progress = entityManager
                    .createQuery(
                            "SELECT p FROM UserChapterProgress p " +
                                    "JOIN FETCH p.user u " +
                                    "JOIN FETCH u.gameState " +
                                    "JOIN FETCH p.chapter c " +
                                    "LEFT JOIN FETCH c.content cnt " +
                                    "LEFT JOIN FETCH cnt.questions q " +
                                    "WHERE u.id = :userId AND c.id = :chapterId " +
                                    "AND p.isDeleted = false", UserChapterProgress.class)
                    .setParameter("userId", userId)
                    .setParameter("chapterId", chapterId)
                    .getSingleResult();
            return Optional.of(progress);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void flush() {

        entityManager.flush();
    }
}
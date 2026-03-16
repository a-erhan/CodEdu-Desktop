package com.codedu.repositories.implementations;

import com.codedu.models.Achievement;
import com.codedu.repositories.interfaces.AchievementRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Optional;
import jakarta.persistence.EntityManager;

@Repository
@Transactional
public class AchievementRepositoryImpl extends GenericRepositoryImpl<Achievement> implements AchievementRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public AchievementRepositoryImpl() {
        super(Achievement.class);
    }

    @Override
    public Optional<Achievement> findByName(String name) {
        try {
            Achievement achievement = entityManager.createQuery(
                    "SELECT a FROM Achievement a WHERE a.name = :name AND a.isDeleted = false", 
                    Achievement.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(achievement);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

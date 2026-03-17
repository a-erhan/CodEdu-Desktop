package com.codedu.repositories.implementations;

import com.codedu.models.DailyChallenge;
import com.codedu.repositories.interfaces.DailyChallengeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class DailyChallengeRepositoryImpl extends GenericRepositoryImpl<DailyChallenge> implements DailyChallengeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public DailyChallengeRepositoryImpl() {
        super(DailyChallenge.class);
    }

    @Override
    public Optional<DailyChallenge> findByName(String name) {
        try {
            DailyChallenge dc = entityManager.createQuery(
                    "SELECT d FROM DailyChallenge d WHERE d.name = :name AND d.isDeleted = false", 
                    DailyChallenge.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(dc);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

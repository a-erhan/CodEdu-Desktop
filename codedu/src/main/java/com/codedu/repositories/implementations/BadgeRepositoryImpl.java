package com.codedu.repositories.implementations;

import com.codedu.models.Badge;
import com.codedu.repositories.interfaces.BadgeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class BadgeRepositoryImpl extends GenericRepositoryImpl<Badge> implements BadgeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public BadgeRepositoryImpl() {
        super(Badge.class);
    }

    @Override
    public Optional<Badge> findByTitle(String title) {
        try {
            Badge badge = entityManager.createQuery(
                    "SELECT b FROM Badge b WHERE b.title = :title AND b.isDeleted = false", 
                    Badge.class)
                    .setParameter("title", title)
                    .getSingleResult();
            return Optional.of(badge);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

package com.codedu.repositories.implementations;

import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.repositories.interfaces.LeaderBoardRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class LeaderBoardRepositoryImpl extends GenericRepositoryImpl<LeaderBoard> implements LeaderBoardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public LeaderBoardRepositoryImpl() {
        super(LeaderBoard.class);
    }

    @Override
    public Optional<LeaderBoard> findByName(String name) {
        try {

            LeaderBoard lb = entityManager.createQuery(
                    "SELECT l FROM LeaderBoard l LEFT JOIN FETCH l.competitors WHERE l.name = :name AND l.isDeleted = false",
                    LeaderBoard.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(lb);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
package com.codedu.repositories.implementations;

import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserRepositoryImpl extends GenericRepositoryImpl<User> implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findByUsernameWithAchievements(String username) {
        try {
            User user = entityManager
                    .createQuery(
                            "SELECT u FROM User u " +
                                    "LEFT JOIN FETCH u.gameState gs " +
                                    "LEFT JOIN FETCH gs.achievements " +
                                    "WHERE u.username = :username AND u.isDeleted = false",
                            User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            User user = entityManager
                    .createQuery(
                            "SELECT u FROM User u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email)) AND u.isDeleted = false",
                            User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            User user = entityManager
                    .createQuery("SELECT u FROM User u WHERE u.username = :username AND u.isDeleted = false",
                            User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(u) FROM User u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email)) AND u.isDeleted = false",
                        Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.isDeleted = false",
                        Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public Optional<User> findByIdWithInventoryAndGameState(int id) {
        return entityManager
                .createQuery(
                        "SELECT DISTINCT u FROM User u " +
                                "LEFT JOIN FETCH u.gameState gs " +
                                "LEFT JOIN FETCH u.inventory inv " +
                                "LEFT JOIN FETCH inv.items ii " +
                                "LEFT JOIN FETCH ii.item it " +
                                "WHERE u.id = :id AND u.isDeleted = false",
                        User.class
                )
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<User> findByUsernameWithInventoryAndGameState(String username) {
        return entityManager
                .createQuery(
                        "SELECT DISTINCT u FROM User u " +
                                "LEFT JOIN FETCH u.gameState gs " +
                                "LEFT JOIN FETCH u.inventory inv " +
                                "LEFT JOIN FETCH inv.items ii " +
                                "LEFT JOIN FETCH ii.item it " +
                                "WHERE u.username = :username AND u.isDeleted = false",
                        User.class
                )
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<User> findAllActiveWithCompetitorAndGameState() {
        return entityManager.createQuery(
                "SELECT DISTINCT u FROM User u " +
                        "LEFT JOIN FETCH u.gameState gs " +
                        "LEFT JOIN FETCH u.competitor c " +
                        "WHERE u.isDeleted = false",
                User.class)
                .getResultList();
    }

    @Override
    public Optional<User> findByIdWithGameStateAndAchievements(int id) {
        try {
            User user = entityManager
                    .createQuery(
                            "SELECT DISTINCT u FROM User u " +
                                    "LEFT JOIN FETCH u.gameState gs " +
                                    "LEFT JOIN FETCH gs.achievements " +
                                    "WHERE u.id = :id AND u.isDeleted = false",
                            User.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

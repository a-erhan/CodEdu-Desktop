package com.codedu.repositories.implementations;

import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

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
    public Optional<User> findByEmail(String email) {
        try {
            User user = entityManager
                    .createQuery("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty(); // Returns empty if no user is found with this email
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
        Long count = entityManager.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = entityManager.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }
}

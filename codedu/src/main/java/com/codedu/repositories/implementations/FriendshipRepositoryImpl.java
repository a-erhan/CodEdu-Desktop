package com.codedu.repositories.implementations;

import com.codedu.models.user.Friendship;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.FriendshipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FriendshipRepositoryImpl extends GenericRepositoryImpl<Friendship> implements FriendshipRepository {

    public FriendshipRepositoryImpl() {
        super(Friendship.class);
    }

    @Override
    public Optional<Friendship> findFriendshipBetween(User user1, User user2) {
        String jpql = "SELECT f FROM Friendship f WHERE " +
                "((f.requester = :user1 AND f.receiver = :user2) OR " +
                "(f.requester = :user2 AND f.receiver = :user1)) AND f.isDeleted = false";
        try {
            Friendship friendship = getEntityManager().createQuery(jpql, Friendship.class)
                    .setParameter("user1", user1)
                    .setParameter("user2", user2)
                    .getSingleResult();
            return Optional.of(friendship);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Friendship> findAcceptedFriendships(User user) {
        String jpql = "SELECT f FROM Friendship f JOIN FETCH f.requester JOIN FETCH f.receiver WHERE " +
                "(f.requester = :user OR f.receiver = :user) AND f.status = :status AND f.isDeleted = false";
        return getEntityManager().createQuery(jpql, Friendship.class)
                .setParameter("user", user)
                .setParameter("status", Friendship.FriendshipStatus.ACCEPTED)
                .getResultList();
    }

    @Override
    public List<Friendship> findPendingRequests(User receiver) {
        String jpql = "SELECT f FROM Friendship f JOIN FETCH f.requester WHERE " +
                "f.receiver = :receiver AND f.status = :status AND f.isDeleted = false";
        return getEntityManager().createQuery(jpql, Friendship.class)
                .setParameter("receiver", receiver)
                .setParameter("status", Friendship.FriendshipStatus.PENDING)
                .getResultList();
    }
}
package com.codedu.repositories.interfaces;

import com.codedu.models.user.Friendship;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends GenericRepository<Friendship> {
    Optional<Friendship> findFriendshipBetween(User user1, User user2);

    List<Friendship> findAcceptedFriendships(User user);

    List<Friendship> findPendingRequests(User receiver);
}

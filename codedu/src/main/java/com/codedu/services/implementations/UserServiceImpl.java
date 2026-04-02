package com.codedu.services.implementations;

import com.codedu.dtos.UserProfileDTO;
import com.codedu.models.user.UserGameState;
import com.codedu.services.interfaces.UserService;
import com.codedu.models.social.Friendship;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.FriendshipRepository;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public UserServiceImpl(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional
    public boolean changePassword(User user, String oldPassword, String newPassword) {

        if (user.getPassword() == null || !user.getPassword().equals(oldPassword)) {
            return false;
        }

        user.setPassword(newPassword);

        userRepository.update(user);

        return true;
    }

    @Transactional
    public void deleteUser(User user) {
        if (user == null || (user.getId()) == 0) {
            throw new IllegalArgumentException("Cannot find any user to delete");
        }

        user.setDeleted(true);
        userRepository.update(user);
    }

    @Transactional
    public void saveUser(User user) {
        if (user != null) {
            userRepository.update(user);
        }
    }

    @Transactional
    public void sendFriendRequest(User requester, User receiver) {
        // Check if a friendship already exists
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requester, receiver);
        if (existing.isPresent()) {
            return; // Already exists (pending, accepted, or blocked)
        }

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Friendship.FriendshipStatus.PENDING)
                .build();
        friendshipRepository.save(friendship);
    }

    @Transactional(readOnly = true)
    public List<User> getAcceptedFriends(User user) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(user);
        return friendships.stream()
                .map(f -> f.getRequester().getId() == user.getId() ? f.getReceiver() : f.getRequester())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getPendingRequests(User receiver) {
        List<Friendship> requests = friendshipRepository.findPendingRequests(receiver);
        return requests.stream()
                .map(Friendship::getRequester)
                .collect(Collectors.toList());
    }

    @Transactional
    public void answerFriendRequest(User receiver, User requester, boolean accept) {
        Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetween(requester, receiver);
        if (friendshipOpt.isPresent()) {
            Friendship friendship = friendshipOpt.get();
            if (friendship.getStatus() == Friendship.FriendshipStatus.PENDING &&
                    friendship.getReceiver().getId() == receiver.getId()) {

                if (accept) {
                    friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
                    friendshipRepository.update(friendship);
                } else {
                    // Rejecting simply deletes the pending request
                    friendship.setDeleted(true);
                    friendshipRepository.update(friendship);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public String getRelationStatus(User currentUser, User targetUser) {
        if (currentUser.getId() == targetUser.getId()) {
            return "SELF";
        }
        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetween(currentUser, targetUser);
        if (friendship.isEmpty()) {
            return "NONE";
        }
        return friendship.get().getStatus().name(); // "PENDING", "ACCEPTED", "BLOCKED"
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String targetUsername, String currentUsername) {
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int level = 1;
        int xp = 0;
        int xpToNext = 1000;
        int tokens = 0;
        int itemsCount = 0;

        if (targetUser.getGameState() != null) {
            level = targetUser.getGameState().getLevel();
            xp = targetUser.getGameState().getXp();
            xpToNext = targetUser.getGameState().getXpToNextLevel();
            tokens = targetUser.getGameState().getTokenBalance();
        }

        if (targetUser.getInventory() != null && targetUser.getInventory().getItems() != null) {
            itemsCount = targetUser.getInventory().getItems().size();
        }

        String relation = "NONE";
        if (targetUsername.equals(currentUsername)) {
            relation = "SELF";
        }

        return UserProfileDTO.builder()
                .userId((long) targetUser.getId())
                .username(targetUser.getUsername())
                .level(level)
                .xp(xp)
                .xpToNextLevel(xpToNext)
                .tokenBalance(tokens)
                .totalItemsOwned(itemsCount)
                .relationStatus(relation)
                .badges(new ArrayList<>())
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithProfileData(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User u = userOpt.get();

            // 🚀 THE FIX: Touch a real data field (not just the ID) to force Hibernate to load the data!
            if (u.getGameState() != null) {
                u.getGameState().getXp(); // Forces the LAZY load to happen right now!
            }
            if (u.getInventory() != null && u.getInventory().getItems() != null) {
                u.getInventory().getItems().size(); // Forces the LAZY items list to load!
            }
            if (u.getCompetitor() != null) {
                u.getCompetitor().getUserRank(); // Forces the LAZY load!
            }
        }
        return userOpt;
    }

    // 🚀 THE FIX: A dedicated, writable transaction just for game economy!
    @Transactional
    public User awardXpAndTokens(String username, int xpReward, int tokenReward) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            UserGameState state = user.getGameState();

            // If they don't have a GameState yet, create one
            if (state == null) {
                state = UserGameState.builder()
                        .user(user).level(1).xp(0).tokenBalance(0).heartCount(3).build();
                user.setGameState(state);
            }

            // Add the new rewards
            state.setXp(state.getXp() + xpReward);
            state.setTokenBalance(state.getTokenBalance() + tokenReward);

            // Save it to the database
            userRepository.update(user);

            // Touch the XP to force Hibernate to load it before closing the connection
            user.getGameState().getXp();
        }
        return user;
    }
}

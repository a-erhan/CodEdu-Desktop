package com.codedu.services.implementations;

import com.codedu.dtos.UserProfileDTO;
import com.codedu.dtos.user.UserDTO;
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
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        if (user.getPassword() == null || !user.getPassword().equals(oldPassword)) {
            return false;
        }

        user.setPassword(newPassword);
        userRepository.update(user);
        return true;
    }

    @Transactional
    public void deleteUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find any user to delete"));
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
    public void sendFriendRequest(int requesterId, int receiverId) {
        User requester = userRepository.findById(requesterId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requester, receiver);
        if (existing.isPresent()) {
            return;
        }

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Friendship.FriendshipStatus.PENDING)
                .build();
        friendshipRepository.save(friendship);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAcceptedFriends(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();

        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(user);
        return friendships.stream()
                .map(f -> f.getRequester().getId() == user.getId() ? f.getReceiver() : f.getRequester())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getPendingRequests(int userId) {
        User receiver = userRepository.findById(userId).orElse(null);
        if (receiver == null) return List.of();

        List<Friendship> requests = friendshipRepository.findPendingRequests(receiver);
        return requests.stream()
                .map(Friendship::getRequester)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void answerFriendRequest(int receiverId, int requesterId, boolean accept) {
        User receiver = userRepository.findById(receiverId).orElseThrow();
        User requester = userRepository.findById(requesterId).orElseThrow();

        Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetween(requester, receiver);
        if (friendshipOpt.isPresent()) {
            Friendship friendship = friendshipOpt.get();
            if (friendship.getStatus() == Friendship.FriendshipStatus.PENDING &&
                    friendship.getReceiver().getId() == receiverId) {

                if (accept) {
                    friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
                    friendshipRepository.update(friendship);
                } else {
                    friendship.setDeleted(true);
                    friendshipRepository.update(friendship);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public String getRelationStatus(int currentUserId, int targetUserId) {
        if (currentUserId == targetUserId) {
            return "SELF";
        }
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        User targetUser = userRepository.findById(targetUserId).orElse(null);
        if (currentUser == null || targetUser == null) return "NONE";

        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetween(currentUser, targetUser);
        if (friendship.isEmpty()) {
            return "NONE";
        }
        return friendship.get().getStatus().name();
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
        return userRepository.findByUsername(username).map(u -> {
            if (u.getGameState() != null) {
                org.hibernate.Hibernate.initialize(u.getGameState());
                u.getGameState().getTokenBalance();
                u.getGameState().getXp();
            }
            return u;
        });
    }

    @Transactional
    public UserDTO awardXpAndTokens(String username, int xpReward, int tokenReward) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            UserGameState state = user.getGameState();

            if (state == null) {
                state = UserGameState.builder()
                        .user(user).level(1).xp(0).tokenBalance(0).heartCount(3).build();
                user.setGameState(state);
            }

            state.setXp(state.getXp() + xpReward);
            state.setTokenBalance(state.getTokenBalance() + tokenReward);

            userRepository.update(user);

            user.getGameState().getXp();
            return toDTO(user);
        }
        return null;
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAcceptedFriendEntities(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(user);
        return friendships.stream()
                .map(f -> f.getRequester().getId() == user.getId() ? f.getReceiver() : f.getRequester())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getPendingRequestEntities(int userId) {
        User receiver = userRepository.findById(userId).orElse(null);
        if (receiver == null) return List.of();
        List<Friendship> requests = friendshipRepository.findPendingRequests(receiver);
        return requests.stream()
                .map(Friendship::getRequester)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User awardXpAndTokensEntity(String username, int xpReward, int tokenReward) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            UserGameState state = user.getGameState();
            if (state == null) {
                state = UserGameState.builder()
                        .user(user).level(1).xp(0).tokenBalance(0).heartCount(3).build();
                user.setGameState(state);
            }
            state.setXp(state.getXp() + xpReward);
            state.setTokenBalance(state.getTokenBalance() + tokenReward);
            userRepository.update(user);
            return user;
        }
        return null;
    }
}

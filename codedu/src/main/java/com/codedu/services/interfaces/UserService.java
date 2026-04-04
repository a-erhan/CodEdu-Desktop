package com.codedu.services.interfaces;

import com.codedu.dtos.UserProfileDTO;
import com.codedu.dtos.user.UserDTO;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    boolean changePassword(int userId, String oldPassword, String newPassword);

    void deleteUser(int userId);

    void saveUser(User user);

    void sendFriendRequest(int requesterId, int receiverId);

    List<UserDTO> getAcceptedFriends(int userId);

    List<UserDTO> getPendingRequests(int userId);

    /** Entity versions for controllers still using entity classes */
    List<User> getAcceptedFriendEntities(int userId);

    List<User> getPendingRequestEntities(int userId);

    void answerFriendRequest(int receiverId, int requesterId, boolean accept);

    String getRelationStatus(int currentUserId, int targetUserId);

    UserProfileDTO getUserProfile(String targetUsername, String currentUsername);

    Optional<User> getUserWithProfileData(String username);

    UserDTO awardXpAndTokens(String username, int xpReward, int tokenReward);

    /** Entity version for controllers still using entity classes */
    User awardXpAndTokensEntity(String username, int xpReward, int tokenReward);

    Optional<User> loadUserForPublicProfile(int userId);

    User decrementHeart(String username);
}

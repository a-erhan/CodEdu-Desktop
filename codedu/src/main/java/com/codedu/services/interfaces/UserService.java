package com.codedu.services.interfaces;

import com.codedu.dtos.UserProfileDTO;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    boolean changePassword(User user, String oldPassword, String newPassword);

    void deleteUser(User user);

    void saveUser(User user);

    void sendFriendRequest(User requester, User receiver);

    List<User> getAcceptedFriends(User user);

    List<User> getPendingRequests(User receiver);

    void answerFriendRequest(User receiver, User requester, boolean accept);

    String getRelationStatus(User currentUser, User targetUser);

    UserProfileDTO getUserProfile(String targetUsername, String currentUsername);

    Optional<User> getUserWithProfileData(String username);
}

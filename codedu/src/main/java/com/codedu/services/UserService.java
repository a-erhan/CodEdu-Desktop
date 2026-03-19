package com.codedu.services;

import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}

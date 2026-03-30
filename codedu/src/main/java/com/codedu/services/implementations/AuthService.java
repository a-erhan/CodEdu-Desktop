package com.codedu.services.implementations;

import com.codedu.models.user.User;
import com.codedu.models.user.Role;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Handles the user registration process.
     */
    public String register(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            return "ERROR: This email is already registered.";
        }
        if (userRepository.existsByUsername(username)) {
            return "ERROR: This username is already taken.";
        }

        // Using the Builder pattern from your User model
        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        userRepository.save(newUser);
        return "SUCCESS";
    }

    /**
     * Logs the user in using the custom validation method defined in the User entity.
     */
    public User login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Calling the login validation method YOU wrote inside User.java
            if (user.login(email, password)) {
                return user; // Login successful
            }
        }

        return null; // Login failed (wrong email, wrong password, or inactive account)
    }
}
package com.codedu.services.implementations;

import com.codedu.models.user.User;
import com.codedu.services.interfaces.AuthService;
import com.codedu.models.user.Role;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Derives a unique username from the email (avoids collisions on the local part only, e.g. two "john@" accounts).
     */
    private static String usernameFromEmail(String normalizedEmail) {
        return normalizedEmail.replace("@", "_at_").replace(".", "_");
    }

    /**
     * Handles the user registration process.
     */
    @Override
    @Transactional
    public String register(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty()) {
            return "ERROR: Email is required.";
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            return "ERROR: This email is already registered.";
        }
        String finalUsername = usernameFromEmail(normalizedEmail);
        if (userRepository.existsByUsername(finalUsername)) {
            return "ERROR: This username is already taken.";
        }

        User newUser = User.builder()
                .username(finalUsername)
                .email(normalizedEmail)
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
    @Override
    @Transactional(readOnly = true)
    public User login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty()) {
            return null;
        }
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.login(normalizedEmail, password)) {
                return user;
            }
        }

        return null;
    }
}
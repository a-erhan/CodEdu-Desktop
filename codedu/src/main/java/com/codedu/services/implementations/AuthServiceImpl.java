package com.codedu.services.implementations;

import com.codedu.dtos.user.UserDTO;
import com.codedu.dtos.user.UserLoginDTO;
import com.codedu.dtos.user.UserRegisterDTO;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
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

    private static String usernameFromEmail(String normalizedEmail) {
        return normalizedEmail.replace("@", "_at_").replace(".", "_");
    }

    @Override
    @Transactional
    public String register(UserRegisterDTO dto) {
        String normalizedEmail = normalizeEmail(dto.email());
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
                .password(dto.password())
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        newUser.setGameState(UserGameState.newDefault());
        userRepository.save(newUser);
        return "SUCCESS";
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO login(UserLoginDTO dto) {
        String normalizedEmail = normalizeEmail(dto.email());
        if (normalizedEmail.isEmpty()) {
            return null;
        }
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.login(normalizedEmail, dto.password())) {
                return toDTO(user);
            }
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
}
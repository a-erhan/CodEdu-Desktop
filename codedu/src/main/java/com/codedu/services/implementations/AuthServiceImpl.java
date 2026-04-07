package com.codedu.services.implementations;

import com.codedu.config.AppMailProperties;
import com.codedu.config.AuthProperties;
import com.codedu.dtos.user.LoginResult;
import com.codedu.dtos.user.UserDTO;
import com.codedu.dtos.user.UserLoginDTO;
import com.codedu.dtos.user.UserRegisterDTO;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.models.user.Role;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.AuthService;
import com.codedu.services.interfaces.VerificationEmailService;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationEmailService verificationEmailService;
    private final AppMailProperties appMailProperties;
    private final MailProperties mailProperties;
    private final AuthProperties authProperties;

    public AuthServiceImpl(UserRepository userRepository,
            VerificationEmailService verificationEmailService,
            AppMailProperties appMailProperties,
            MailProperties mailProperties,
            AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.verificationEmailService = verificationEmailService;
        this.appMailProperties = appMailProperties;
        this.mailProperties = mailProperties;
        this.authProperties = authProperties;
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Local part of the email (before {@code @}); used as the basis for the stored username. */
    private static String localPartFromEmail(String normalizedEmail) {
        int at = normalizedEmail.indexOf('@');
        if (at <= 0) {
            return "user";
        }
        String local = normalizedEmail.substring(0, at);
        return local.isBlank() ? "user" : local;
    }

    private String uniqueUsernameFromEmail(String normalizedEmail) {
        String base = localPartFromEmail(normalizedEmail);
        String candidate = base;
        int n = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + n;
            n++;
        }
        return candidate;
    }

    private String verificationLinkForToken(String token) {
        String base = authProperties.getVerificationBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/api/auth/verify-email?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private void assignVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiresAt(Instant.now().plus(
                authProperties.getVerificationTokenValidHours(), ChronoUnit.HOURS));
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
        String finalUsername = uniqueUsernameFromEmail(normalizedEmail);

        User newUser = User.builder()
                .username(finalUsername)
                .email(normalizedEmail)
                .password(dto.password())
                .role(Role.STUDENT)
                .isActive(true)
                .emailVerified(false)
                .build();

        newUser.setGameState(UserGameState.newDefault());

        if (appMailProperties.isSkipSend()) {
            newUser.setEmailVerified(true);
            userRepository.save(newUser);
            return "SUCCESS_SKIP_EMAIL";
        }

        if (!isMailConfiguredForSending()) {
            return "ERROR: Email verification is not configured. Set MAIL_USERNAME and MAIL_APP_PASSWORD (Gmail app password), or enable app.mail.skip-send for local development.";
        }

        assignVerificationToken(newUser);
        userRepository.save(newUser);

        try {
            verificationEmailService.sendVerificationEmail(normalizedEmail, verificationLinkForToken(newUser.getEmailVerificationToken()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not send verification email.", e);
        }

        return "SUCCESS";
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(UserLoginDTO dto) {
        String normalizedEmail = normalizeEmail(dto.email());
        if (normalizedEmail.isEmpty()) {
            return LoginResult.fail("Invalid email or password.");
        }
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        if (userOptional.isEmpty()) {
            return LoginResult.fail("Invalid email or password.");
        }

        User user = userOptional.get();
        if (user.getPassword() == null || !user.getPassword().equals(dto.password())) {
            return LoginResult.fail("Invalid email or password.");
        }
        if (!user.isActive()) {
            return LoginResult.fail("This account is disabled.");
        }
        if (!user.isEmailVerified()) {
            return LoginResult.fail("Please verify your email using the link we sent you. You can resend the email from the login screen.");
        }

        return LoginResult.ok(toDTO(user));
    }

    @Override
    @Transactional
    public String verifyEmailToken(String token) {
        if (token == null || token.isBlank()) {
            return "ERROR: Invalid verification link.";
        }
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token.trim());
        if (userOpt.isEmpty()) {
            return "ERROR: Invalid or unknown verification link.";
        }
        User user = userOpt.get();
        if (user.getEmailVerificationExpiresAt() != null
                && Instant.now().isAfter(user.getEmailVerificationExpiresAt())) {
            return "ERROR: This verification link has expired. Use “Resend verification email” on the login screen.";
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);
        userRepository.update(user);
        return "SUCCESS";
    }

    @Override
    @Transactional
    public String resendVerificationEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty()) {
            return "ERROR: Enter your email address.";
        }
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            return "ERROR: No account found with that email.";
        }
        User user = userOpt.get();
        if (user.isEmailVerified()) {
            return "ERROR: This email is already verified. You can sign in.";
        }
        if (appMailProperties.isSkipSend()) {
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationExpiresAt(null);
            userRepository.update(user);
            return "SUCCESS";
        }
        if (!isMailConfiguredForSending()) {
            return "ERROR: Email is not configured. Set MAIL_USERNAME and MAIL_APP_PASSWORD.";
        }

        assignVerificationToken(user);
        userRepository.update(user);

        try {
            verificationEmailService.sendVerificationEmail(normalizedEmail, verificationLinkForToken(user.getEmailVerificationToken()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not send verification email.", e);
        }
        return "SUCCESS";
    }

    private boolean isMailConfiguredForSending() {
        String user = mailProperties.getUsername();
        String pass = mailProperties.getPassword();
        return user != null && !user.isBlank() && pass != null && !pass.isBlank();
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}

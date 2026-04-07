package com.codedu.services.implementations;

import com.codedu.config.AppMailProperties;
import com.codedu.services.interfaces.VerificationEmailService;
import jakarta.mail.MessagingException;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class VerificationEmailServiceImpl implements VerificationEmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final AppMailProperties appMailProperties;

    public VerificationEmailServiceImpl(JavaMailSender mailSender,
            MailProperties mailProperties,
            AppMailProperties appMailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.appMailProperties = appMailProperties;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        if (appMailProperties.isSkipSend()) {
            return;
        }
        String from = mailProperties.getUsername();
        if (from == null || from.isBlank()) {
            throw new IllegalStateException("spring.mail.username is not configured.");
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Verify your CodEdu account");
            helper.setText(buildHtml(verificationLink), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send verification email.", e);
        }
    }

    private static String buildHtml(String verificationLink) {
        return """
                <p>Thanks for signing up for CodEdu.</p>
                <p><a href="%s">Verify your email address</a> to activate your account.</p>
                <p>If you did not create an account, you can ignore this message.</p>
                """.formatted(verificationLink);
    }
}

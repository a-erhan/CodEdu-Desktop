package com.codedu.services.interfaces;

public interface VerificationEmailService {

    void sendVerificationEmail(String toEmail, String verificationLink);
}

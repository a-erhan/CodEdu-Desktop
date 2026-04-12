package com.codedu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private String verificationBaseUrl = "http://localhost:8080";

    private int verificationTokenValidHours = 48;

    public String getVerificationBaseUrl() {
        return verificationBaseUrl;
    }

    public void setVerificationBaseUrl(String verificationBaseUrl) {
        this.verificationBaseUrl = verificationBaseUrl;
    }

    public int getVerificationTokenValidHours() {
        return verificationTokenValidHours;
    }

    public void setVerificationTokenValidHours(int verificationTokenValidHours) {
        this.verificationTokenValidHours = verificationTokenValidHours;
    }
}

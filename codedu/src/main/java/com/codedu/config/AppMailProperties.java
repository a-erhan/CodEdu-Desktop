package com.codedu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

    /**
     * When true, verification emails are not sent and dev flows can auto-verify (see {@code AuthServiceImpl}).
     */
    private boolean skipSend = false;

    public boolean isSkipSend() {
        return skipSend;
    }

    public void setSkipSend(boolean skipSend) {
        this.skipSend = skipSend;
    }
}

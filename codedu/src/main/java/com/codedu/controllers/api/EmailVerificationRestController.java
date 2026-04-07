package com.codedu.controllers.api;

import com.codedu.services.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class EmailVerificationRestController {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationRestController.class);

    private final AuthService authService;

    public EmailVerificationRestController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmail(@RequestParam(value = "token", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(400).body(htmlPage(false,
                    escapeHtml("Missing token. Use the full link from your email.")));
        }
        try {
            String result = authService.verifyEmailToken(token);
            if ("SUCCESS".equals(result)) {
                return ResponseEntity.ok(htmlPage(true, "Email verified. You can sign in to CodEdu."));
            }
            String message = result.startsWith("ERROR: ") ? result.substring("ERROR: ".length()) : result;
            return ResponseEntity.status(400).body(htmlPage(false, escapeHtml(message)));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.status(500).body(htmlPage(false,
                    escapeHtml("Something went wrong while verifying your email. Please try again or request a new link from the app.")));
        }
    }

    private static String escapeHtml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String htmlPage(boolean ok, String message) {
        String color = ok ? "#a3be8c" : "#bf616a";
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="utf-8"><title>CodEdu — Email verification</title></head>
                <body style="font-family: system-ui, sans-serif; background:#2e3440; color:#eceff4; padding:2rem;">
                <h1 style="color:%s;">%s</h1>
                <p>%s</p>
                </body>
                </html>
                """.formatted(color, ok ? "Success" : "Could not verify", message);
    }
}

package com.codedu.services;

import com.codedu.models.user.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIChatbotService {
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private static final String API_KEY = "API_KEY";

    private final RestTemplate restTemplate;

    public AIChatbotService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends a prompt to the Gemini API and returns the AI's text response.
     */
    public String askAi(String prompt) {
        String url = GEMINI_URL + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Constructing the JSON payload expected by the Gemini API
        // Format: { "contents": [{ "parts": [{"text": "your prompt here"}] }] }
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text",
                "You are a java tutor, always use Java language; you should help the people in the CodEdu learning platform. Try to use the ideas of user instead of generating from scratch. You should provide the full runnable solution and explain it pedagogically"
                        + prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(parts));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Send the POST request
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Parse the JSON response to extract the actual text
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
                    return (String) resParts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "AI CONNECTION ERROR: " + e.getMessage();
        }

        return "I am sorry, I couldn't process that request right now.";
    }
}

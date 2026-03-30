package com.codedu.services.implementations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIChatbotService {

    @Value("${gemini.url}")
    private String geminiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public AIChatbotService() {
        this.restTemplate = new RestTemplate();
    }

    public String askAi(String prompt) {
        String url = geminiUrl + "?key=" + apiKey;

        System.out.println("geminiUrl = " + geminiUrl);
        System.out.println("apiKey = " + apiKey);
        System.out.println("final url = " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put(
                "text",
                "You are a Java tutor, always use Java language. " +
                        "You should help people in the CodEdu learning platform. " +
                        "Try to use the user's ideas instead of generating from scratch. " +
                        "Provide a full runnable solution and explain it pedagogically.\n\n" +
                        prompt
        );

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> responseContent = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> responseParts = (List<Map<String, Object>>) responseContent.get("parts");
                    if (responseParts != null && !responseParts.isEmpty()) {
                        return (String) responseParts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "AI CONNECTION ERROR: " + e.getMessage();
        }

        return "I am sorry, I couldn't process that request right now.";
    }
}
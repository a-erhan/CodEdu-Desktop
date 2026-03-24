package com.codedu.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodeExecutionService {

    private static final String JDOODLE_URL = "https://api.jdoodle.com/v1/execute";

    private static final String CLIENT_ID = "7da6169882cd9ffe322f57e4bc8ad9d6";
    private static final String CLIENT_SECRET = "380c588ef361353aec4cb3839e8c9a4a4cfd477ff58b0e0a545aacb2dd16f858";

    private final RestTemplate restTemplate;

    public CodeExecutionService() {
        this.restTemplate = new RestTemplate();
    }

    public String executeJavaCode(String sourceCode, String input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clientId", CLIENT_ID);
        requestBody.put("clientSecret", CLIENT_SECRET);
        requestBody.put("script", sourceCode);
        requestBody.put("language", "java");
        requestBody.put("versionIndex", "4");

        if (input != null && !input.trim().isEmpty()) {
            requestBody.put("stdin", input);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(JDOODLE_URL, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String output = (String) responseBody.get("output");

                if (output != null) {
                    if (output.contains("error:") || output.contains("Exception in thread")) {
                        return "RUNTIME/COMPILE ERROR:\n" + output.trim();
                    }
                    return output.trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "CONNECTION ERROR: " + e.getMessage();
        }

        return "";
    }
}
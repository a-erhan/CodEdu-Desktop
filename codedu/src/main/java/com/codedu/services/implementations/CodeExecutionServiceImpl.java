package com.codedu.services.implementations;

import com.codedu.services.interfaces.CodeExecutionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodeExecutionServiceImpl implements CodeExecutionService {

    @Value("${app.jdoodle.url}")
    private String jdoodleUrl;

    @Value("${app.jdoodle.client-id}")
    private String clientId;

    @Value("${app.jdoodle.client-secret}")
    private String clientSecret;
    private final RestTemplate restTemplate;

    public CodeExecutionServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    public String executeJavaCode(String sourceCode, String input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clientId", clientId);
        requestBody.put("clientSecret", clientSecret);
        requestBody.put("script", sourceCode);
        requestBody.put("language", "java");
        requestBody.put("versionIndex", "4");

        if (input != null && !input.trim().isEmpty()) {
            requestBody.put("stdin", input);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(jdoodleUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String output = (String) responseBody.get("output");

                if (output != null) {
                    if (output.contains("error:") || output.contains("Exception in thread")
                            || output.contains("standard error")) {
                        return "EXECUTION ERROR:\n" + output.trim();
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
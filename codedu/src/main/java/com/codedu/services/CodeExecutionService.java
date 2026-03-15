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

    private static final String JUDGE0_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true";
    private final RestTemplate restTemplate;

    public CodeExecutionService() {
        this.restTemplate = new RestTemplate();
    }

    public String executeJavaCode(String sourceCode, String input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //RapidAPI keys will be entered here
        headers.set("x-rapidapi-host", "judge0-ce.p.rapidapi.com");
        headers.set("x-rapidapi-key", "API_KEY_WILL_BE_HERE");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source_code", sourceCode);
        requestBody.put("language_id", 62);
        requestBody.put("stdin", input);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(JUDGE0_URL, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String stderr = (String) responseBody.get("stderr");
                String compileOutput = (String) responseBody.get("compile_output");

                if (stderr != null && !stderr.trim().isEmpty()) {
                    return "RUNTIME ERROR:\n" + stderr;
                }
                if (compileOutput != null && !compileOutput.trim().isEmpty()) {
                    return "COMPILE ERROR:\n" + compileOutput;
                }

                String stdout = (String) responseBody.get("stdout");
                return stdout != null ? stdout.trim() : "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "CONNECTION ERROR";
        }

        return "";
    }
}

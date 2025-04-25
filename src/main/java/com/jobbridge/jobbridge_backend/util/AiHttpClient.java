package com.jobbridge.jobbridge_backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiHttpClient {
    private final RestTemplate restTemplate;
    private static final String AI_MATCH_URL = "http://localhost:5001/api/match";

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getMatches(
            String resume, List<String> jobListings, List<Long> jobIds) {

        Map<String,Object> req = Map.of(
                "resume", resume,
                "job_listings", jobListings,
                "job_ids", jobIds
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(req, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity(AI_MATCH_URL, entity, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("AI 서버 오류: " + resp.getStatusCode());
        }
        return (List<Map<String,Object>>) resp.getBody().get("results");
    }
}

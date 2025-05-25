package com.jobbridge.jobbridge_backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiHttpClient {
    private final RestTemplate restTemplate;

    private static final String AI_MATCH_URL = "http://localhost:5001/api/match";
    private static final String AI_CAREER_URL = "http://localhost:5001/api/career-path";

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMatches(String resume, List<String> jobListings, List<Long> jobIds) {
        Map<String, Object> req = Map.of(
                "resume", resume,
                "job_listings", jobListings,
                "job_ids", jobIds
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(AI_MATCH_URL, entity, Map.class);
            return (List<Map<String, Object>>) resp.getBody().get("results");
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new RuntimeException("AI 매칭 오류: " + ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            throw new RuntimeException("AI 서버 연결 실패: " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("예기치 못한 오류 발생: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getCareerPath(String resume, String jobDescription) {
        Map<String, Object> req = Map.of(
                "resume", resume,
                "job_description", jobDescription
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(AI_CAREER_URL, entity, Map.class);
            return (List<String>) resp.getBody().get("recommendations");
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new RuntimeException("경력 추천 오류: " + ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            throw new RuntimeException("AI 서버 연결 실패: " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("예기치 못한 오류 발생: " + ex.getMessage());
        }
    }
}
package com.myanatomy.notesapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * GeminiService – Integrates Google Gemini AI for note analysis.
 *
 * Responsibilities:
 *  1. Build a structured prompt (prompt engineering)
 *  2. Call the Gemini REST API via RestTemplate
 *  3. Parse the JSON response
 *  4. Return { summary, tags } to NoteService
 *
 * We inject the API key from application.properties — never hardcode secrets!
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyzes a note and returns AI-generated summary and tags.
     *
     * @param title   Note title
     * @param content Note content
     * @return Map containing "summary" (String) and "tags" (List<String>)
     */
    public Map<String, Object> analyzeNote(String title, String content) {
        log.debug("Calling Gemini AI for note: {}", title);

        String prompt = buildPrompt(title, content);
        Map<String, Object> requestBody = buildRequestBody(prompt);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            String text = extractTextFromResponse(response.getBody());
            log.debug("Gemini raw response: {}", text);

            return parseJsonResponse(text);

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return Map.of(
                "summary", "AI analysis temporarily unavailable. Please try again.",
                "tags", List.of()
            );
        }
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Builds a structured prompt using prompt engineering best practices:
     * - Defines the role (you are a note assistant)
     * - Specifies exact output format (JSON)
     * - Sets constraints (no markdown, no extra text)
     * - Provides example structure
     */
    private String buildPrompt(String title, String content) {
        return """
            You are a smart note-taking assistant. Analyze the following note.
            
            Respond ONLY with valid JSON. No markdown. No code blocks. No extra text.
            Use this exact format:
            {"summary": "2-3 sentence summary here", "tags": ["tag1", "tag2", "tag3"]}
            
            Rules:
            - summary: 2-3 clear, informative sentences
            - tags: 3-5 single-word lowercase topic tags
            - tags should represent main concepts, technologies, or categories
            
            Note Title: %s
            Note Content: %s
            """.formatted(title, content != null ? content : "(no content)");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of("contents", List.of(
            Map.of("parts", List.of(Map.of("text", prompt)))
        ));
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<?, ?> responseBody) {
        try {
            var candidates = (List<Map<?, ?>>) responseBody.get("candidates");
            var firstCandidate = candidates.get(0);
            var content = (Map<?, ?>) firstCandidate.get("content");
            var parts = (List<Map<?, ?>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Could not extract text from Gemini response: {}", e.getMessage());
            return "{\"summary\": \"Could not parse AI response\", \"tags\": []}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String jsonText) {
        try {
            // Strip any markdown code blocks Gemini might add despite our instructions
            String cleaned = jsonText.trim()
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
            return objectMapper.readValue(cleaned, Map.class);
        } catch (Exception e) {
            log.error("JSON parse failed. Raw text: {}", jsonText);
            return Map.of("summary", jsonText, "tags", List.of());
        }
    }
}


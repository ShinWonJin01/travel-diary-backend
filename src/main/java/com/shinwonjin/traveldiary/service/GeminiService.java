package com.shinwonjin.traveldiary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class GeminiService {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1/interactions";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    public GeminiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API 키가 설정되지 않았습니다."
            );
        }

        try {
            String requestBody = createRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Gemini API 요청에 실패했습니다. status="
                                + response.statusCode()
                );
            }

            return extractText(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Gemini API 요청이 중단되었습니다.",
                    exception
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Gemini API 호출 중 오류가 발생했습니다.",
                    exception
            );
        }
    }

    private String createRequestBody(String prompt) throws Exception {
        var body = objectMapper.createObjectNode();

        body.put("model", model);
        body.put("input", prompt);
        body.put("store", false);

        return objectMapper.writeValueAsString(body);
    }

    private String extractText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode steps = root.get("steps");

        if (steps == null || !steps.isArray()) {
            throw new IllegalStateException(
                    "Gemini 응답에서 결과를 찾을 수 없습니다."
            );
        }

        for (int i = steps.size() - 1; i >= 0; i--) {
            JsonNode step = steps.get(i);

            if (!"model_output".equals(step.path("type").asText())) {
                continue;
            }

            JsonNode content = step.get("content");
            if (content == null || !content.isArray()) continue;

            for (JsonNode item : content) {
                if (!"text".equals(item.path("type").asText())) continue;

                String text = item.path("text").asText();
                if (!text.isBlank()) return text;
            }
        }

        throw new IllegalStateException(
                "Gemini 응답에서 텍스트를 찾을 수 없습니다."
        );
    }
}
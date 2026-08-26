package com.example.news.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GeminiAiProvider implements AiProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAiProvider(
            RestClient.Builder builder,
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-3.6-flash}") String model) {
        this.restClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            throw new IllegalStateException("AI is not configured. Set GEMINI_API_KEY.");
        }

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of(
                        "maxOutputTokens", 2048,
                        "thinkingConfig", Map.of("thinkingLevel", "MINIMAL")
                )
        );

        GeminiResponse response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new IllegalStateException("AI provider returned no content");
        }

        Candidate candidate = response.candidates().getFirst();
        if ("MAX_TOKENS".equals(candidate.finishReason())) {
            throw new IllegalStateException("AI response exceeded the output limit. Please try again.");
        }
        if (candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
            throw new IllegalStateException("AI provider returned an empty response");
        }

        String text = candidate.content().parts().stream()
                .map(Part::text)
                .filter(part -> part != null && !part.isBlank())
                .reduce((left, right) -> left + "\n" + right)
                .orElseThrow(() -> new IllegalStateException("AI provider returned an empty response"));

        if (text.isBlank()) {
            throw new IllegalStateException("AI provider returned an empty response");
        }
        return text;
    }

    @Override
    public String modelName() {
        return model;
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private record GeminiResponse(List<Candidate> candidates) {}
    private record Candidate(Content content, String finishReason) {}
    private record Content(List<Part> parts) {}
    private record Part(String text) {}
}

package com.example.news.client;

import com.example.news.model.NewsApiResult;
import com.example.news.model.NewsArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Component
public class GuardianClient {

    private static final Logger log = LoggerFactory.getLogger(GuardianClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // Base URL for Guardian API
    private final String BASE_URL = "https://content.guardianapis.com/search";

    @Value("${guardian.api.key}")
    private String apiKey;

    public NewsApiResult search(String keyword, int page, int pageSize) {
        // Return empty list for empty API KEY
        if (apiKey == null || apiKey.isEmpty()) {
            log.debug("Guardian API key is missing");
            return new NewsApiResult(0, 0, Collections.emptyList());
        }
        try {
            int currentPage = Math.max(page, 1);
            int size = Math.max(pageSize, 10);

            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", keyword)
                    .queryParam("page", currentPage)
                    .queryParam("page-size", size)
                    .queryParam("show-fields", "thumbnail,trailText")
                    .queryParam("api-key", apiKey)
                    .toUriString();

            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("response")) {
                log.warn("Empty or invalid Guardian API response");
                return new NewsApiResult(0, 0, Collections.emptyList());
            }

            // Extract main response section
            Map<String, Object> responseData = (Map<String, Object>) response.get("response");

            // total results
            int totalResults = (int) responseData.getOrDefault("total", 0);
            int totalPages = (int) responseData.getOrDefault("pages", 0);

            // Get "results" array from "response"
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseData.getOrDefault("results", Collections.emptyList());
            List<NewsArticle> articles = new ArrayList<>();
            for (Map<String, Object> r : results) {
                String title = (String) r.getOrDefault("webTitle", null);
                String webUrl = (String) r.getOrDefault("webUrl", null);
                String date = (String) r.getOrDefault("webPublicationDate", null);
                Map<String, Object> fields = (Map<String, Object>) r.get("fields");
                String imageUrl = fields != null ? (String) fields.get("thumbnail") : null;
                String description = fields != null ? (String) fields.get("trailText") : null;

                // Build and collect NewsArticle
                articles.add(new NewsArticle(title, description, webUrl, "The Guardian", date, imageUrl));
            }
            return new NewsApiResult(totalResults, totalPages, articles);

        } catch (RestClientException ex) {
            log.warn("Guardian API error: {}", ex.getMessage());
            return new NewsApiResult(0, 0, Collections.emptyList());
        }
    }
}

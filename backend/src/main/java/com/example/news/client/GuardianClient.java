package com.example.news.client.guardian;

import com.example.news.client.NewsProviderClient;
import com.example.news.model.NewsApiResult;
import com.example.news.model.NewsArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuardianClient implements NewsProviderClient {

    private final com.example.news.client.guardian.GuardianFeignClient feignClient;

    @Value("${guardian.api.key}")
    private String apiKey;

    @Override
    public String getProviderName() {
        return "Guardian";
    }

    @Override
    public NewsApiResult search(String keyword, int page, int pageSize) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Guardian API key missing");
            return emptyResult();
        }

        try {
            Map<String, Object> response =
                    feignClient.search(
                            keyword, Math.max(page, 1),
                            Math.max(pageSize, 10), "thumbnail,trailText",
                            "newest", "published", apiKey
                    );

            Map<String, Object> responseData = (Map<String, Object>) response.get("response");

            int totalResults = (int) responseData.getOrDefault("total", 0);
            int totalPages = (int) responseData.getOrDefault("pages", 0);

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) responseData.getOrDefault("results", Collections.emptyList());

            List<NewsArticle> articles = new ArrayList<>();

            for (Map<String, Object> r : results) {
                Map<String, Object> fields = (Map<String, Object>) r.get("fields");

                articles.add(new NewsArticle(
                        (String) r.get("webTitle"),
                        fields != null ? (String) fields.get("trailText") : null,
                        (String) r.get("webUrl"),
                        "The Guardian",
                        (String) r.get("webPublicationDate"),
                        fields != null ? (String) fields.get("thumbnail") : null
                ));
            }

            return new NewsApiResult(totalResults, totalPages, articles);

        } catch (Exception ex) {
            log.error("Guardian API error", ex);
            return emptyResult();
        }
    }

    private NewsApiResult emptyResult() {
        return new NewsApiResult(0, 0, Collections.emptyList());
    }
}
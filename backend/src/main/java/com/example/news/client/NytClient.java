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
public class NytClient {

    private static final Logger log = LoggerFactory.getLogger(NytClient.class);
    private static final String BASE_URL = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${nyt.api.key}")
    private String apiKey;

    public NewsApiResult search(String keyword, int page, int pageSize) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("NYT API key is missing");
            return new NewsApiResult(0, 0, Collections.emptyList());
        }
        try {
            int currentPage = Math.max(0, page - 1);
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", keyword)
                    .queryParam("page", currentPage)
                    .queryParam("api-key", apiKey)
                    .toUriString();

            Map<?, ?> responseMap = restTemplate.getForObject(url, Map.class);
            if (responseMap == null || !responseMap.containsKey("response")) {
                log.debug("NYT returned no response for '{}'", keyword);
                return new NewsApiResult(0, 0, Collections.emptyList());
            }

            Map<String, Object> response = (Map<String, Object>) responseMap.get("response");
            Map<String, Object> meta = (Map<String, Object>) response.getOrDefault("meta", Collections.emptyMap());

            int totalResults = (int) meta.getOrDefault("hits", 0);
            int totalPages = (int) Math.ceil(totalResults / (double) pageSize);

            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.getOrDefault("docs", Collections.emptyList());

            List<NewsArticle> articles = new ArrayList<>(docs.size());
            for (Map<String, Object> doc : docs) {
                // ✅ Extract title
                Map<String, Object> headline = (Map<String, Object>) doc.get("headline");
                String title = headline != null ? (String) headline.getOrDefault("main", null) : null;

                // ✅ Extract description/snippet
                String description = (String) doc.getOrDefault("abstract", doc.get("snippet"));

                // ✅ Extract URL & published date
                String webUrl = (String) doc.getOrDefault("web_url", null);
                String publishedAt = (String) doc.getOrDefault("pub_date", null);

                // ✅ Extract image URL (thumbnail preferred)
                String imageUrl = null;
                try {
                    Map<String, Object> multimedia = (Map<String, Object>) doc.get("multimedia");
                    if (multimedia != null && multimedia.containsKey("thumbnail")) {
                        Map<String, Object> thumb = (Map<String, Object>) multimedia.get("thumbnail");
                        imageUrl = (String) thumb.getOrDefault("url", null);
                    } else if (multimedia != null && multimedia.containsKey("default")) {
                        Map<String, Object> def = (Map<String, Object>) multimedia.get("default");
                        imageUrl = (String) def.getOrDefault("url", null);
                    }
                } catch (ClassCastException ignored) {
                }

                // ✅ Fallback if no image found
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = "https://placehold.co/600x400?text=News+Image";
                }

                articles.add(new NewsArticle(
                        title,
                        description,
                        webUrl,
                        "The New York Times",
                        publishedAt,
                        imageUrl
                ));
            }
            return new NewsApiResult(totalResults, totalPages, articles);
        } catch (RestClientException ex) {
            log.warn("NYTimes API error: {}", ex.getMessage());
            return new NewsApiResult(0, 0, Collections.emptyList());
        }
    }
}

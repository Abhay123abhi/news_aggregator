package com.example.news.client.nyt;

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
public class NytClient implements NewsProviderClient {

    private final com.example.news.client.nyt.NytFeignClient feignClient;

    @Value("${nyt.api.key}")
    private String apiKey;

    @Override
    public String getProviderName() {
        return "NYT";
    }

    @Override
    public NewsApiResult search(String keyword, int page, int pageSize) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("NYT API key missing");
            return emptyResult();
        }

        try {
            Map<String, Object> responseMap =
                    feignClient.search(keyword, Math.max(0, page - 1), apiKey);

            Map<String, Object> response =
                    (Map<String, Object>) responseMap.get("response");

            Map<String, Object> meta =
                    (Map<String, Object>) response.getOrDefault("meta", Collections.emptyMap());

            int totalResults = (int) meta.getOrDefault("hits", 0);
            int totalPages = (int) Math.ceil(totalResults / (double) pageSize);

            List<Map<String, Object>> docs =
                    (List<Map<String, Object>>) response.getOrDefault("docs", Collections.emptyList());

            List<NewsArticle> articles = new ArrayList<>();

            for (Map<String, Object> doc : docs) {
                Map<String, Object> headline = (Map<String, Object>) doc.get("headline");

                Map<String, Object> multimedia =
                        (Map<String, Object>) doc.get("multimedia");

                String imageUrl = null;

                if (multimedia != null) {
                    Map<String, Object> defaultImage =
                            (Map<String, Object>) multimedia.get("default");

                    if (defaultImage != null) {
                        imageUrl = (String) defaultImage.get("url");
                    }
                }

                if (imageUrl == null || imageUrl.isBlank()) {
                    imageUrl = "https://placehold.co/600x400?text=No+Image";
                }

                articles.add(new NewsArticle(
                        headline != null ? (String) headline.get("main") : null,
                        (String) doc.getOrDefault("abstract", doc.get("snippet")),
                        (String) doc.get("web_url"),
                        "The New York Times",
                        (String) doc.get("pub_date"),
                        imageUrl
                ));
            }

            return new NewsApiResult(totalResults, totalPages, articles);

        } catch (Exception ex) {
            log.error("NYT API error", ex);
            return emptyResult();
        }
    }

    private NewsApiResult emptyResult() {
        return new NewsApiResult(0, 0, Collections.emptyList());
    }
}
package com.example.news.service;

import com.example.news.client.NewsProviderClient;
import com.example.news.model.NewsApiResult;
import com.example.news.model.NewsArticle;
import com.example.news.model.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final List<NewsProviderClient> providers;
    private final CacheService cacheService;

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    @Value("${news.guardian.enabled:true}")
    private boolean guardianEnabled;

    @Value("${news.nyt.enabled:true}")
    private boolean nytEnabled;

    public AggregationService(List<NewsProviderClient> providers, CacheService cacheService) {
        this.providers = providers;
        this.cacheService = cacheService;
    }

    public SearchResponse search(String keyword, int page, int pageSize, boolean offline) {

        Instant startTime = Instant.now();

        String searchQuery = (keyword == null || keyword.isBlank()) ? "latest" : keyword.trim();
        int currentPage = (page < 1) ? DEFAULT_PAGE : page;
        int size = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;

        List<NewsArticle> allArticles = new ArrayList<>();
        boolean usedOffline = false;

        if (offline) {
            allArticles = cacheService.load(searchQuery);
            usedOffline = true;
            log.info("Offline mode requested; loaded {} cached articles", allArticles.size());
        } else {

            // Filter enabled providers dynamically
            List<NewsProviderClient> activeProviders = providers.stream()
                    .filter(p -> isProviderEnabled(p.getProviderName()))
                    .toList();

            List<CompletableFuture<NewsApiResult>> futures = activeProviders.stream()
                    .map(provider -> CompletableFuture.supplyAsync(
                            () -> provider.search(searchQuery, currentPage, size), executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            for (CompletableFuture<NewsApiResult> future : futures) {
                try {
                    NewsApiResult result = future.join();
                    allArticles.addAll(result.articles());
                } catch (Exception ex) {
                    log.warn("Provider failed: {}", ex.getMessage());
                }
            }

            if (allArticles.isEmpty()) {
                log.warn("No online results found. Using offline cache");
                allArticles = cacheService.load(searchQuery);
                usedOffline = true;
            } else {
                cacheService.save(searchQuery, allArticles);
            }
        }

        // Deduplicate by normalized URL
        List<NewsArticle> uniqueArticles = allArticles.stream()
                .filter(a -> a.url() != null && !a.url().isBlank())
                .collect(Collectors.toMap(
                        a -> normalizeUrl(a.url()),
                        a -> a,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values().stream()
                .sorted(Comparator.comparing(
                        NewsArticle::publishedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(size)
                .toList();

        long timeTaken = Duration.between(startTime, Instant.now()).toMillis();

        return new SearchResponse(
                "News Aggregator",
                searchQuery,
                "Global",
                currentPage,
                size,
                uniqueArticles.size(),
                currentPage + 1,
                currentPage > 1 ? currentPage - 1 : null,
                currentPage + 1,
                usedOffline,
                timeTaken,
                uniqueArticles
        );
    }

    private boolean isProviderEnabled(String providerName) {
        return switch (providerName.toLowerCase()) {
            case "guardian" -> guardianEnabled;
            case "nyt" -> nytEnabled;
            default -> true;
        };
    }

    private String normalizeUrl(String url) {
        String s = url.trim().toLowerCase();
        int queryIdx = s.indexOf('?');
        if (queryIdx > 0) s = s.substring(0, queryIdx);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }
}
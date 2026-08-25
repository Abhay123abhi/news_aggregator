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

    private final ExecutorService providerExecutor;

    @Value("${news.provider-timeout}")
    private Duration providerTimeout;

    @Value("${news.guardian.enabled:true}")
    private boolean guardianEnabled;

    @Value("${news.nyt.enabled:true}")
    private boolean nytEnabled;

    public AggregationService(List<NewsProviderClient> providers, CacheService cacheService,
                              ExecutorService providerExecutor) {
        this.providers = providers;
        this.cacheService = cacheService;
        this.providerExecutor = providerExecutor;
    }

    public SearchResponse search(String keyword, int page, int pageSize, boolean offline) {

        Instant startTime = Instant.now();

        String searchQuery = (keyword == null || keyword.isBlank()) ? "latest" : keyword.trim();
        int currentPage = (page < 1) ? DEFAULT_PAGE : page;
        int size = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, 25);

        List<NewsArticle> allArticles = new ArrayList<>();
        int totalAvailableArticles = 0;
        int availablePages = currentPage;
        boolean usedOffline = false;

        if (offline) {
            NewsApiResult cached = loadCachedResult(searchQuery, currentPage, size);

            if (cached != null) {
                allArticles.addAll(cached.articles());
                usedOffline = true;
            }

            log.info("Offline mode requested; loaded {} cached articles", allArticles.size());
        } else {
            NewsApiResult cached = loadCachedResult(searchQuery, currentPage, size);

            if (cached != null && !cached.articles().isEmpty()) {
                allArticles.addAll(cached.articles());
                totalAvailableArticles = cached.totalResults();
                availablePages = Math.max(currentPage, cached.totalPages());
                log.info("Cache hit for keyword {}, page {}, page size {}", searchQuery, currentPage, size);
            } else {
                List<NewsProviderClient> activeProviders = providers.stream()
                        .filter(provider -> isProviderEnabled(provider.getProviderName()))
                        .toList();

                List<CompletableFuture<NewsApiResult>> futures = activeProviders.stream()
                        .map(provider -> CompletableFuture.supplyAsync(
                                        () -> provider.search(searchQuery, currentPage, size), providerExecutor)
                                .completeOnTimeout(emptyResult(), providerTimeout.toMillis(), TimeUnit.MILLISECONDS)
                                .exceptionally(ex -> {
                                    log.warn("Provider {} failed", provider.getProviderName(), ex);
                                    return emptyResult();
                                }))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                for (CompletableFuture<NewsApiResult> future : futures) {
                    NewsApiResult result = future.join();
                    allArticles.addAll(result.articles());
                    totalAvailableArticles += result.totalResults();
                    availablePages = Math.max(availablePages, result.totalPages());
                }

                if (!allArticles.isEmpty()) {
                    saveCachedResult(searchQuery, currentPage, size,
                            new NewsApiResult(totalAvailableArticles, availablePages, List.copyOf(allArticles)));
                } else {
                    NewsApiResult fallback = loadCachedResult(searchQuery, currentPage, size);
                    if (fallback != null && !fallback.articles().isEmpty()) {
                        log.warn("Using cached results for keyword {} after provider failures", searchQuery);
                        allArticles.addAll(fallback.articles());
                        usedOffline = true;
                    }
                }
            }
        }

        // Providers return a page each. Pagination metadata therefore reflects this aggregated page.
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
        int totalArticles = usedOffline ? uniqueArticles.size()
                : Math.max(totalAvailableArticles, uniqueArticles.size());
        int totalPages = usedOffline ? currentPage : availablePages;
        Integer nextPage = !usedOffline && currentPage < totalPages ? currentPage + 1 : null;

        return new SearchResponse(
                "News Aggregator",
                searchQuery,
                "Global",
                currentPage,
                size,
                totalArticles,
                totalPages,
                currentPage > 1 ? currentPage - 1 : null,
                nextPage,
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

    private NewsApiResult emptyResult() {
        return new NewsApiResult(0, 0, List.of());
    }

    private NewsApiResult loadCachedResult(String keyword, int page, int pageSize) {
        try {
            return cacheService.load(keyword, page, pageSize);
        } catch (RuntimeException ex) {
            log.warn("Unable to read cached articles for keyword {}", keyword, ex);
            return null;
        }
    }

    private void saveCachedResult(String keyword, int page, int pageSize, NewsApiResult result) {
        try {
            cacheService.save(keyword, page, pageSize, result);
        } catch (RuntimeException ex) {
            log.warn("Unable to cache articles for keyword {}", keyword, ex);
        }
    }
}

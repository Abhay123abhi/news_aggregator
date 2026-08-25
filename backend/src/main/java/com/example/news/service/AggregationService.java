package com.example.news.service;

import com.example.news.client.NewsProviderClient;
import com.example.news.exception.NewsUnavailableException;
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

    public SearchResponse search(String keyword, int page, int pageSize) {

        Instant startTime = Instant.now();

        String searchQuery = normalizeSearchKeyword(keyword);
        String providerQuery = "latest".equals(searchQuery) ? null : searchQuery;
        int currentPage = (page < 1) ? DEFAULT_PAGE : page;
        int size = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, 25);

        List<NewsArticle> allArticles = new ArrayList<>();
        int totalAvailableArticles = 0;
        int availablePages = currentPage;

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

            if (activeProviders.isEmpty()) {
                throw new NewsUnavailableException("No news provider is enabled");
            }

            List<ProviderTask> providerTasks = activeProviders.stream()
                    .map(provider -> new ProviderTask(
                            provider.getProviderName(),
                            CompletableFuture.supplyAsync(
                                            () -> provider.search(providerQuery, currentPage, size),
                                            providerExecutor
                                    )
                                    .orTimeout(providerTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    ))
                    .toList();

            int successfulProviders = 0;
            int timedOutProviders = 0;

            for (ProviderTask task : providerTasks) {
                try {
                    NewsApiResult result = task.future().join();
                    successfulProviders++;
                    allArticles.addAll(result.articles());
                    totalAvailableArticles += result.totalResults();
                    availablePages = Math.max(availablePages, result.totalPages());
                } catch (CompletionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    if (cause instanceof TimeoutException) {
                        timedOutProviders++;
                        log.warn("Provider {} timed out after {} ms",
                                task.providerName(), providerTimeout.toMillis());
                    } else {
                        String message = cause.getMessage() == null
                                ? cause.getClass().getSimpleName()
                                : cause.getMessage();
                        log.warn("Provider {} failed: {}", task.providerName(), message);
                    }
                }
            }

            if (successfulProviders == 0) {
                if (timedOutProviders == activeProviders.size()) {
                    throw new NewsUnavailableException(
                            "All news providers timed out after " + providerTimeout.toSeconds()
                                    + " seconds. Check upstream connectivity or increase NEWS_PROVIDER_TIMEOUT."
                    );
                }
                throw new NewsUnavailableException(
                        "No news provider completed successfully. Configure GUARDIAN_API_KEY or NYT_API_KEY and check the server logs."
                );
            }

            if (!allArticles.isEmpty()) {
                saveCachedResult(searchQuery, currentPage, size,
                        new NewsApiResult(totalAvailableArticles, availablePages, List.copyOf(allArticles)));
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
        int totalArticles = Math.max(totalAvailableArticles, uniqueArticles.size());
        int totalPages = availablePages;
        Integer nextPage = currentPage < totalPages ? currentPage + 1 : null;

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

    private String normalizeSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "latest";
        }

        String normalized = keyword.trim();
        return normalized.equalsIgnoreCase("latest-news") || normalized.equalsIgnoreCase("latest")
                ? "latest"
                : normalized;
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

    private record ProviderTask(
            String providerName,
            CompletableFuture<NewsApiResult> future
    ) {
    }
}

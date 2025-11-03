package com.example.news.service;

import com.example.news.client.GuardianClient;
import com.example.news.client.NytClient;
import com.example.news.model.NewsApiResult;
import com.example.news.model.NewsArticle;
import com.example.news.model.SearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String CACHE_DIR = "."; // working dir (change if you want)
    private static final String CACHE_PREFIX = "offline-";
    private static final int FETCH_PAGES = 2;
    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GuardianClient guardianClient;
    @Autowired
    private NytClient nytClient;
    @Autowired
    private CacheService cacheService;
    @Value("${news.guardian.enabled:true}")
    private boolean guardianEnabled;
    @Value("${news.nyt.enabled:true}")
    private boolean nytEnabled;

    public SearchResponse search(String keyword, int page, int pageSize, boolean offline) {
        Instant startTime = Instant.now();

        String searchQuery = (keyword == null || keyword.isBlank()) ? "latest" : keyword.trim();
        int currentPage = (page < 1) ? DEFAULT_PAGE : page;
        int size = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;

        int guardianTotal = 0, guardianPages = 0;
        int nytTotal = 0, nytPages = 0;

        List<NewsArticle> allArticles = new ArrayList<>();
        boolean usedOffline = false;

        if (offline) {
            allArticles = cacheService.load(searchQuery);
            usedOffline = true;
            log.info("Offline mode requested; loaded {} cached articles for '{}'", allArticles.size(), searchQuery);
        } else {
            // Fetch data from Guardian and NYT
            CompletableFuture<NewsApiResult> guardianFuture = guardianEnabled
                    ? CompletableFuture.supplyAsync(() -> guardianClient.search(searchQuery, currentPage, size), executor)
                    : CompletableFuture.completedFuture(new NewsApiResult(0, 0, Collections.emptyList()));

            CompletableFuture<NewsApiResult> nytFuture = nytEnabled
                    ? CompletableFuture.supplyAsync(() -> nytClient.search(searchQuery, currentPage, size), executor)
                    : CompletableFuture.completedFuture(new NewsApiResult(0, 0, Collections.emptyList()));


//            CompletableFuture<List<NewsArticle>> nytFuture = CompletableFuture.supplyAsync(
//                    () -> {
//                        if (nytEnabled) {
//                            try {
//                                int nytPage = Math.max(0, currentPage - 1);
//                                return nytClient.search(searchQuery, nytPage, size);
//                            } catch (Exception e) {
//                                log.warn("NYT API failed: {}", e.getMessage());
//                            }
//                        }
//                        return Collections.emptyList();
//                    }, executor
//            );

            // 🧠 Wait for both to complete in parallel
            CompletableFuture.allOf(guardianFuture, nytFuture).join();

            NewsApiResult guardianResult = guardianFuture.join();
            NewsApiResult nytResult = nytFuture.join();

            guardianTotal = guardianResult.totalResults();
            guardianPages = guardianResult.totalPages();
            nytTotal = nytResult.totalResults();
            nytPages = nytResult.totalPages();

            allArticles.addAll(guardianResult.articles());
            allArticles.addAll(nytResult.articles());

            // Fallback to offline cache if both failed
            if (allArticles.isEmpty()) {
                log.warn("No online results found. Using offline cache for '{}'", searchQuery);
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
                .sorted(Comparator.comparing(NewsArticle::publishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
//        int totalArticles = uniqueArticles.size();
//        int totalPages = (totalArticles == 0) ? 0 : (int) Math.ceil((double) totalArticles / size);
//        Integer prevPage = (currentPage > 1) ? currentPage - 1 : null;
//        Integer nextPage = (currentPage < totalPages) ? currentPage + 1 : null;

        // Compute totals
        int totalArticles = guardianTotal + nytTotal;
        int totalPages = Math.max(guardianPages, nytPages);

        // ✅ Paginate the combined list locally for UI (in case totalPages isn't perfect)
        int fromIndex = Math.min((currentPage - 1) * size, uniqueArticles.size());
        int toIndex = Math.min(fromIndex + size, uniqueArticles.size());
        List<NewsArticle> paginatedArticles = uniqueArticles.subList(fromIndex, toIndex);

        Integer prevPage = (currentPage > 1) ? currentPage - 1 : null;
        Integer nextPage = (currentPage < totalPages) ? currentPage + 1 : null;

        List<NewsArticle> limitedArticles = uniqueArticles.stream().limit(size).toList();

        // Data for UI
        List<String> articleUrls = limitedArticles.stream()
                .map(NewsArticle::url)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<String> headlines = limitedArticles.stream()
                .map(a -> a.title() != null ? a.title() : a.description())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long timeTaken = Duration.between(startTime, Instant.now()).toMillis();

        return new SearchResponse(
                "News Aggregator",
                searchQuery,
                "Global",              // City placeholder (can be dynamic if needed)
                currentPage,
                size,
                totalArticles,
                totalPages,
                prevPage,
                nextPage,
                usedOffline,
                timeTaken,
                limitedArticles,
                articleUrls,
                headlines
        );
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        String s = url.trim().toLowerCase();
        int queryIdx = s.indexOf('?');
        if (queryIdx > 0) s = s.substring(0, queryIdx);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }
}

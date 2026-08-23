package com.example.news.service;

import com.example.news.client.NewsProviderClient;
import com.example.news.model.NewsApiResult;
import com.example.news.model.NewsArticle;
import com.example.news.model.SearchResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AggregationServiceTest {

    @Mock
    private NewsProviderClient provider;

    @Mock
    private CacheService cacheService;

    private ExecutorService executor;
    private AggregationService service;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(2);
        service = new AggregationService(List.of(provider), cacheService, executor);
        ReflectionTestUtils.setField(service, "providerTimeout", Duration.ofSeconds(2));
        ReflectionTestUtils.setField(service, "guardianEnabled", true);
        ReflectionTestUtils.setField(service, "nytEnabled", true);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void returnsProviderTotalsAndNextPageWhenMorePagesExist() {
        NewsArticle article = article("https://example.com/first");
        when(provider.getProviderName()).thenReturn("Guardian");
        when(provider.search("java", 1, 12)).thenReturn(new NewsApiResult(36, 3, List.of(article)));

        SearchResponse response = service.search("java", 1, 12, false);

        assertThat(response.totalArticles()).isEqualTo(36);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.nextPage()).isEqualTo(2);
        verify(cacheService).save("java", 1, 12, List.of(article));
    }

    @Test
    void doesNotExposeNextPageOnLastProviderPage() {
        when(provider.getProviderName()).thenReturn("Guardian");
        when(provider.search("java", 3, 12))
                .thenReturn(new NewsApiResult(36, 3, List.of(article("https://example.com/last"))));

        SearchResponse response = service.search("java", 3, 12, false);

        assertThat(response.prevPage()).isEqualTo(2);
        assertThat(response.nextPage()).isNull();
    }

    @Test
    void fallsBackToCacheWhenProviderReturnsNoArticles() {
        NewsArticle article = article("https://example.com/cached");
        when(provider.getProviderName()).thenReturn("Guardian");
        when(provider.search("java", 1, 12)).thenReturn(new NewsApiResult(0, 0, List.of()));
        when(cacheService.load("java", 1, 12)).thenReturn(List.of(article));

        SearchResponse response = service.search("java", 1, 12, false);

        assertThat(response.offline()).isTrue();
        assertThat(response.articles()).containsExactly(article);
        assertThat(response.nextPage()).isNull();
    }

    private NewsArticle article(String url) {
        return new NewsArticle("Headline", "Description", url, "The Guardian", "2026-08-23T10:00:00Z", null);
    }
}

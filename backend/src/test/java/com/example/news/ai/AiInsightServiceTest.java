package com.example.news.ai;

import com.example.news.model.NewsArticle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AiInsightServiceTest {

    @Test
    void briefUsesProviderAndCachesSameArticleSet() {
        AtomicInteger calls = new AtomicInteger();
        AiProvider provider = new AiProvider() {
            @Override
            public String generate(String systemPrompt, String userPrompt) {
                calls.incrementAndGet();
                assertThat(systemPrompt).contains("Use ONLY the supplied article");
                assertThat(userPrompt).contains("Guardian").contains("Example headline");
                return "Grounded brief";
            }

            @Override
            public String modelName() {
                return "test-model";
            }

            @Override
            public boolean isConfigured() {
                return true;
            }
        };

        AiInsightService service = new AiInsightService(provider, true, 15);
        List<NewsArticle> articles = List.of(new NewsArticle(
                "Example headline", "Example description", "https://example.com/story",
                "Guardian", "2026-08-26T00:00:00Z", null));

        AiInsightService.AiResult first = service.dailyBrief(articles);
        AiInsightService.AiResult second = service.dailyBrief(articles);

        assertThat(first.text()).isEqualTo("Grounded brief");
        assertThat(first.cached()).isFalse();
        assertThat(second.cached()).isTrue();
        assertThat(calls.get()).isEqualTo(1);
    }
}

package com.example.news.ai;

import com.example.news.model.NewsArticle;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Service
public class AiInsightService {

    private static final String SYSTEM_PROMPT = """
            You are the intelligence layer of a news aggregator.
            Use ONLY the supplied article title, description, source and URL.
            Never invent facts, quotes, events or sources.
            If the supplied evidence is insufficient, say so clearly.
            Keep the answer concise, neutral and useful.
            Refer to publishers by name when comparing coverage.
            """;

    private final AiProvider aiProvider;
    private final Map<String, CachedInsight> cache = new ConcurrentHashMap<>();

    public AiInsightService(AiProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    public AiResult summarize(NewsArticle article) {
        String prompt = """
                Summarize this article in 3 short bullet points, then add one line titled 'Why it matters'.

                %s
                """.formatted(formatArticle(article, 1));
        return generate("summary:" + stableKey(article), prompt);
    }

    public AiResult explainWhyItMatters(NewsArticle article) {
        String prompt = """
                Explain why this story matters to a general reader in at most 100 words.
                Separate confirmed information from implications. Do not speculate beyond the supplied article.

                %s
                """.formatted(formatArticle(article, 1));
        return generate("why:" + stableKey(article), prompt);
    }

    public AiResult dailyBrief(List<NewsArticle> articles) {
        List<NewsArticle> limited = safeArticles(articles).stream().limit(8).toList();
        String prompt = """
                Create a compact news briefing from these articles.
                Return:
                1. A 2-sentence overview.
                2. Up to 5 key developments as bullets.
                3. A final 'Watch next' line based only on unresolved developments explicitly visible in the supplied text.

                ARTICLES:
                %s
                """.formatted(formatArticles(limited));
        return generate("brief:" + articlesKey(limited), prompt);
    }

    public AiResult ask(String question, List<NewsArticle> articles) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question is required");
        }
        List<NewsArticle> limited = safeArticles(articles).stream().limit(10).toList();
        String prompt = """
                Answer the user's question using only the supplied articles.
                Keep the answer below 180 words.
                Cite evidence inline using [1], [2], etc. matching the article numbers.
                If the answer is not supported by these articles, say: 'The current news set does not provide enough evidence.'

                QUESTION:
                %s

                ARTICLES:
                %s
                """.formatted(question.trim(), formatArticles(limited));
        return generate("ask:" + Integer.toHexString((question.trim() + articlesKey(limited)).hashCode()), prompt);
    }

    public AiResult compare(List<NewsArticle> articles) {
        List<NewsArticle> limited = safeArticles(articles).stream().limit(8).toList();
        String prompt = """
                Compare how the supplied publishers cover the topic.
                Return these headings only when supported by evidence:
                - Common ground
                - Different emphasis
                - Missing context
                Do not label political bias or intent. Compare observable framing, topics emphasized, and facts included.

                ARTICLES:
                %s
                """.formatted(formatArticles(limited));
        return generate("compare:" + articlesKey(limited), prompt);
    }

    private AiResult generate(String key, String prompt) {
        CachedInsight cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return new AiResult(cached.text(), aiProvider.modelName(), true);
        }

        String result = aiProvider.generate(SYSTEM_PROMPT, prompt).trim();
        cache.put(key, new CachedInsight(result, Instant.now().plus(Duration.ofMinutes(30))));
        return new AiResult(result, aiProvider.modelName(), false);
    }

    private List<NewsArticle> safeArticles(List<NewsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            throw new IllegalArgumentException("At least one article is required");
        }
        return articles.stream().filter(article -> article != null && article.title() != null).toList();
    }

    private String formatArticles(List<NewsArticle> articles) {
        return IntStream.range(0, articles.size())
                .mapToObj(index -> formatArticle(articles.get(index), index + 1))
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
    }

    private String formatArticle(NewsArticle article, int number) {
        return """
                [%d]
                Title: %s
                Source: %s
                Published: %s
                Description: %s
                URL: %s
                """.formatted(
                number,
                clean(article.title()),
                clean(article.source()),
                clean(article.publishedAt()),
                clean(article.description()),
                clean(article.url())
        );
    }

    private String articlesKey(List<NewsArticle> articles) {
        return Integer.toHexString(articles.stream().map(this::stableKey).reduce("", String::concat).hashCode());
    }

    private String stableKey(NewsArticle article) {
        return Integer.toHexString((clean(article.url()) + clean(article.title())).hashCode());
    }

    private String clean(String value) {
        if (value == null) return "Unavailable";
        return value.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    public record AiResult(String text, String model, boolean cached) {}
    private record CachedInsight(String text, Instant expiresAt) {}
}

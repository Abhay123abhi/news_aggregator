package com.example.news.model;

import java.util.List;

public record NewsApiResult(int totalResults,
                            int totalPages,
                            List<NewsArticle> articles) {
}

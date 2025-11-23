package com.example.news.service;

import com.example.news.model.NewsArticle;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CacheService {
    @Cacheable(value = "offlineCache", key = "#keyword")
    public List<NewsArticle> load(String keyword) {
        // If cache miss → AggregationService will fetch from API
        System.out.println("Chache Miss");
        return List.of();
    }

    @CachePut(value = "offlineCache", key = "#keyword")
    public List<NewsArticle> save(String keyword, List<NewsArticle> articles) {
        return articles;
    }
}

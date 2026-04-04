package com.example.news.service;

import com.example.news.model.NewsArticle;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CacheService {
    @Cacheable(value = "offlineCache", key = "#keyword + '_' + #page")
    public List<NewsArticle> load(String keyword, int page) {
        System.out.println("Cache Miss");
        return null;
    }

    @CachePut(value = "offlineCache", key = "#keyword + '_' + #page")
    public List<NewsArticle> save(String keyword, int page, List<NewsArticle> articles) {
        return articles;
    }
}

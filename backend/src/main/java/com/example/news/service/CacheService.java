package com.example.news.service;

import com.example.news.model.NewsArticle;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CacheService {
    @Cacheable(value = "offlineCache", key = "#keyword.toLowerCase() + '_' + #page + '_' + #pageSize", unless = "#result == null")
    public List<NewsArticle> load(String keyword, int page, int pageSize) {
        return null;
    }

    @CachePut(value = "offlineCache", key = "#keyword.toLowerCase() + '_' + #page + '_' + #pageSize")
    public List<NewsArticle> save(String keyword, int page, int pageSize, List<NewsArticle> articles) {
        return articles;
    }
}

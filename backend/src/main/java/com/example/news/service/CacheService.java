package com.example.news.service;

import com.example.news.model.NewsApiResult;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Cacheable(value = "newsSearch", key = "#keyword.toLowerCase() + '_' + #page + '_' + #pageSize", unless = "#result == null")
    public NewsApiResult load(String keyword, int page, int pageSize) {
        return null;
    }

    @CachePut(value = "newsSearch", key = "#keyword.toLowerCase() + '_' + #page + '_' + #pageSize")
    public NewsApiResult save(String keyword, int page, int pageSize, NewsApiResult result) {
        return result;
    }
}

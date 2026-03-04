package com.example.news.client;

import com.example.news.model.NewsApiResult;

public interface NewsProviderClient {
    NewsApiResult search(String keyword, int page, int pageSize);

    String getProviderName();
}
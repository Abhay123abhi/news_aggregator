package com.example.news.model;

public record NewsArticle (
        String title,
        String description,
        String url,
        String source,
        String publishedAt,
        String imageUrl
){}

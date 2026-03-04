package com.example.news.controller;

import com.example.news.model.SearchResponse;
import com.example.news.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private AggregationService service;

    @GetMapping
    public SearchResponse search(
            @RequestParam(defaultValue = "latest-news") String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "false") boolean offline
    ) {
        return service.search(keyword, page, pageSize, offline);
    }
}

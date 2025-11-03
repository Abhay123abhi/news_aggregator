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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false, defaultValue = "false") boolean offline
    ) {
        return service.search(keyword, page, pageSize, offline);
    }
}

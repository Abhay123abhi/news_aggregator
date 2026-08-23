package com.example.news.controller;

import com.example.news.model.SearchResponse;
import com.example.news.service.AggregationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final AggregationService service;

    @GetMapping
    public SearchResponse search(
            @RequestParam(defaultValue = "latest-news") @NotBlank String keyword,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(25) Integer pageSize,
            @RequestParam(defaultValue = "false") boolean offline
    ) {
        return service.search(keyword, page, pageSize, offline);
    }
}

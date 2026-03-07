package com.example.news.service;

import com.example.news.model.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSchedulerService {

    private final AggregationService aggregationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void fetchAndPushLatestNews() {

        try {

            log.info("Running news refresh scheduler");

            SearchResponse response =
                    aggregationService.search("latest-news", 1, 10, false);

            messagingTemplate.convertAndSend("/topic/news", response);

            log.info("News pushed to WebSocket clients");

        } catch (Exception e) {

            log.error("Failed to fetch or push news", e);

        }
    }
}
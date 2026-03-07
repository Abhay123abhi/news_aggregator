package com.example.news.client.guardian;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "guardianFeignClient", url = "https://content.guardianapis.com")
public interface GuardianFeignClient {

    @GetMapping("/search")
    Map<String, Object> search(
            @RequestParam("q") String keyword,
            @RequestParam("page") int page,
            @RequestParam("page-size") int pageSize,
            @RequestParam("show-fields") String fields,
            @RequestParam("order-by") String orderBy,
            @RequestParam("order-date") String orderDate,
            @RequestParam("api-key") String apiKey
    );
}
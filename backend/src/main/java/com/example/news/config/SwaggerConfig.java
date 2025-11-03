package com.example.news.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI newsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                              .title("News Aggregator API")
                              .version("1.0")
                              .description("Aggregates results from Guardian & NYT APIs with pagination and caching."));
    }
}

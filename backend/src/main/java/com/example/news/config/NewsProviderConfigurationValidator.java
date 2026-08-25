package com.example.news.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NewsProviderConfigurationValidator {

    @Value("${news.guardian.enabled:true}")
    private boolean guardianEnabled;

    @Value("${news.nyt.enabled:true}")
    private boolean nytEnabled;

    @Value("${guardian.api.key:}")
    private String guardianApiKey;

    @Value("${nyt.api.key:}")
    private String nytApiKey;

    @PostConstruct
    void validate() {
        boolean guardianConfigured = guardianEnabled && hasText(guardianApiKey);
        boolean nytConfigured = nytEnabled && hasText(nytApiKey);

        if (!guardianConfigured && !nytConfigured) {
            throw new IllegalStateException(
                    "No enabled news provider has an API key. Set GUARDIAN_API_KEY or NYT_API_KEY before starting the backend."
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

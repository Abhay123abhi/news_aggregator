package com.example.news.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NewsProviderConfigurationValidatorTest {

    @Test
    void acceptsOneConfiguredEnabledProvider() {
        NewsProviderConfigurationValidator validator = validator(true, "guardian-key", true, "");

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void rejectsStartupWhenNoEnabledProviderHasAKey() {
        NewsProviderConfigurationValidator validator = validator(true, "", true, "");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GUARDIAN_API_KEY")
                .hasMessageContaining("NYT_API_KEY");
    }

    private NewsProviderConfigurationValidator validator(
            boolean guardianEnabled,
            String guardianKey,
            boolean nytEnabled,
            String nytKey
    ) {
        NewsProviderConfigurationValidator validator = new NewsProviderConfigurationValidator();
        ReflectionTestUtils.setField(validator, "guardianEnabled", guardianEnabled);
        ReflectionTestUtils.setField(validator, "guardianApiKey", guardianKey);
        ReflectionTestUtils.setField(validator, "nytEnabled", nytEnabled);
        ReflectionTestUtils.setField(validator, "nytApiKey", nytKey);
        return validator;
    }
}

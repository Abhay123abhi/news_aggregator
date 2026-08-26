package com.example.news.ai;

public interface AiProvider {
    String generate(String systemPrompt, String userPrompt);
    String modelName();
    boolean isConfigured();
}

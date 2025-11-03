package com.example.news.service;

import com.example.news.model.NewsArticle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class CacheService {

    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${cache.directory:./cache}")
    private String cacheDir;
    @Value("${cache.ttl.hours:6}")
    private int ttlHours;

    public void save(String keyword, List<NewsArticle> articles) {
        File file = file(keyword);
        file.getParentFile().mkdirs();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            mapper.writeValue(writer, Map.of("timestamp", Instant.now().toString(), "articles", articles));
        } catch (IOException ignored) {
        }
    }

    @Cacheable(value = "offlineCache", key = "#keyword")
    public List<NewsArticle> load(String keyword) {
        File file = file(keyword);
        if (!file.exists() || isExpired(file)) return Collections.emptyList();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            Map<String, Object> cache = mapper.readValue(
                    reader, new TypeReference<>() {
                    }
            );
            return mapper.convertValue(
                    cache.get("articles"), new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private boolean isExpired(File f) {
        long ageMs = System.currentTimeMillis() - f.lastModified();
        long ttlMs = ttlHours * 3600 * 1000L;
        return ageMs > ttlMs;
    }

    @Scheduled(fixedRate = 3600000) // every hour
    public void cleanOldFiles() {
        File dir = new File(cacheDir);
        if (!dir.exists()) return;
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (isExpired(f)) f.delete();
        }
    }

    private File file(String keyword) {
        String safe = keyword.replaceAll("[^a-zA-Z0-9_-]", "_");
        return new File(cacheDir, safe + ".json");
    }
}

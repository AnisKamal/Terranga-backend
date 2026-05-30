package com.terranga.service;

import com.terranga.client.GoogleNewsClient;
import com.terranga.dto.NewsArticleResponse;
import com.terranga.entities.NewsArticleEntity;
import com.terranga.repositories.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    private final GoogleNewsClient googleNewsClient;
    private final NewsArticleRepository repository;

    @Transactional
    @CacheEvict(value = "news-list", allEntries = true)
    public void syncArticles() {
        log.info("=== Début sync actualités Google News ===");
        List<GoogleNewsClient.NewsRawItem> items = googleNewsClient.fetchSenegalFootballNews();
        log.info("Articles reçus de Google News : {}", items.size());

        if (items.isEmpty()) return;

        // Dédup par guid (même précaution que PlayerService pour éviter StaleStateException)
        Map<String, GoogleNewsClient.NewsRawItem> uniqueByGuid = new LinkedHashMap<>();
        for (GoogleNewsClient.NewsRawItem item : items) {
            if (item.guid() == null) continue;
            uniqueByGuid.putIfAbsent(item.guid(), item);
        }

        for (GoogleNewsClient.NewsRawItem item : uniqueByGuid.values()) {
            repository.findByGuid(item.guid())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setTitle(item.title());
                                existing.setLink(item.link());
                                // NE PAS écraser imageUrl si déjà enrichie
                                if (existing.getImageUrl() == null && item.imageUrl() != null) {
                                    existing.setImageUrl(item.imageUrl());
                                }
                                existing.setSource(item.source());
                                existing.setPublishedTimestamp(item.publishedTimestamp());
                                existing.setExcerpt(item.excerpt());
                                repository.save(existing);
                            },
                            () -> repository.save(toEntity(item))
                    );
        }

        long total = repository.count();
        log.info("=== Fin sync actualités : {} articles en DB ===", total);
    }

    @Cacheable(value = "news-list", key = "'all'")
    public List<NewsArticleResponse> getAllArticles() {
        return repository.findAllOrderedByPublishedDesc().stream()
                .map(this::toDto)
                .toList();
    }

    private NewsArticleEntity toEntity(GoogleNewsClient.NewsRawItem raw) {
        NewsArticleEntity e = new NewsArticleEntity();
        e.setGuid(raw.guid());
        e.setTitle(raw.title());
        e.setLink(raw.link());
        e.setImageUrl(raw.imageUrl());
        e.setSource(raw.source());
        e.setPublishedTimestamp(raw.publishedTimestamp());
        e.setExcerpt(raw.excerpt());
        return e;
    }

    private NewsArticleResponse toDto(NewsArticleEntity e) {
        String publishedAt = e.getPublishedTimestamp() == null
                ? null
                : Instant.ofEpochSecond(e.getPublishedTimestamp()).atOffset(ZoneOffset.UTC).toString();
        return new NewsArticleResponse(
                e.getGuid(),
                e.getTitle(),
                e.getLink(),
                e.getImageUrl(),
                e.getSource(),
                publishedAt,
                e.getExcerpt()
        );
    }
}

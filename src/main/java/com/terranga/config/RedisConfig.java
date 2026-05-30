package com.terranga.config;

import com.terranga.dto.MatchesViewResponse;
import com.terranga.dto.NewsArticleResponse;
import com.terranga.dto.PlayerResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cache Redis avec sérialiseurs typés par cache.
 *
 * Pourquoi cette approche ?
 *  - {@link JacksonJsonRedisSerializer} prend une Class/JavaType au constructeur
 *  - Pas de polymorphic typing nécessaire → pas besoin de @class dans le JSON
 *  - Les records Java (final) fonctionnent sans modification
 *
 * Chaque @Cacheable est lié à un cache logique, qui a son propre serializer
 * typé sur la valeur de retour exacte de la méthode cachée.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // TypeFactory partagé pour construire les types génériques (List<X>)
        TypeFactory typeFactory = JsonMapper.builder().build().getTypeFactory();

        JavaType listOfPlayer = typeFactory.constructCollectionType(List.class, PlayerResponse.class);
        JavaType listOfNews = typeFactory.constructCollectionType(List.class, NewsArticleResponse.class);

        Map<String, RedisCacheConfiguration> perCache = Map.of(
                "matches-view", cacheConfig(
                        new JacksonJsonRedisSerializer<>(MatchesViewResponse.class),
                        Duration.ofMinutes(5)),
                "players-list", cacheConfig(
                        new JacksonJsonRedisSerializer<>(listOfPlayer),
                        Duration.ofHours(1)),
                "news-list", cacheConfig(
                        new JacksonJsonRedisSerializer<>(listOfNews),
                        Duration.ofMinutes(15))
        );

        // Défaut conservateur si on oublie de déclarer un cache plus tard (TTL 10 min, valeurs String)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(SerializationPair.fromSerializer(RedisSerializer.string()));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    /** Construit une config Redis par cache avec un sérialiseur de valeur dédié. */
    private static RedisCacheConfiguration cacheConfig(
            RedisSerializer<?> valueSerializer, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer));
    }
}

package com.terranga.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Client NewsAPI.org pour récupérer les actualités foot Sénégal.
 *
 * Avantages par rapport au scraping RSS :
 * - URLs publisher directes (pas de redirection Google News)
 * - Images directes via urlToImage (pas besoin de scraping og:image)
 * - Snippet (description) ready-to-use
 * - JSON simple, pas de parsing XML
 *
 * Quota free tier : 100 req/jour, articles des 30 derniers jours.
 *
 * (Nom de classe historique "GoogleNewsClient" conservé pour ne pas casser
 *  l'injection dans NewsService — pourra être renommé en NewsApiClient plus tard.)
 */
@Component
public class GoogleNewsClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleNewsClient.class);

    @Value("${api.newsapi.key:}")
    private String apiKey;

    @Value("${api.newsapi.query:senegal}")
    private String query;

    @Value("${api.newsapi.language:fr}")
    private String language;

    private final RestClient restClient = RestClient.create();

    public List<NewsRawItem> fetchSenegalFootballNews() {
        log.info("Fetch NewsAPI : qInTitle={}, language={}", query, language);
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("NEWSAPI_KEY non configurée — sync news désactivée");
            return List.of();
        }
        try {
            NewsApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("newsapi.org")
                            .path("/v2/everything")
                            // qInTitle = match dans le titre uniquement (article vraiment sur le sujet)
                            .queryParam("qInTitle", query)
                            .queryParam("language", language)
                            .queryParam("sortBy", "publishedAt")
                            .queryParam("pageSize", 50)
                            .build())
                    .header("X-Api-Key", apiKey)
                    .header("User-Agent", "TerrangaApp/1.0")
                    .retrieve()
                    .body(NewsApiResponse.class);

            if (response == null) {
                log.warn("Réponse NewsAPI null");
                return List.of();
            }
            if (!"ok".equalsIgnoreCase(response.status())) {
                log.warn("Status NewsAPI != ok : {} — {}", response.status(), response.message());
                return List.of();
            }
            List<NewsApiArticle> articles = response.articles() != null ? response.articles() : List.of();
            log.info("NewsAPI : {} articles reçus (totalResults={})", articles.size(), response.totalResults());

            List<NewsRawItem> items = new ArrayList<>();
            for (NewsApiArticle a : articles) {
                if (a.url() == null || a.title() == null) continue;
                items.add(toRawItem(a));
            }
            return items;
        } catch (Exception e) {
            log.error("Échec fetch NewsAPI", e);
            return List.of();
        }
    }

    private NewsRawItem toRawItem(NewsApiArticle a) {
        // guid = URL de l'article (unique chez NewsAPI)
        String guid = a.url();
        // publishedAt est ISO-8601 ("2024-01-15T10:30:00Z") → Unix seconds
        Long ts = null;
        if (a.publishedAt() != null && !a.publishedAt().isBlank()) {
            try {
                ts = OffsetDateTime.parse(a.publishedAt()).toEpochSecond();
            } catch (Exception ignored) {
                log.debug("Date NewsAPI non parsable : {}", a.publishedAt());
            }
        }
        String sourceName = a.source() != null ? a.source().name() : null;
        // L'excerpt = description si présente, sinon le content tronqué
        String excerpt = a.description() != null ? a.description() : a.content();
        excerpt = truncate(excerpt, 500);

        return new NewsRawItem(guid, a.title(), a.url(), a.urlToImage(), sourceName, ts, excerpt);
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max - 1).trim() + "…";
    }

    // --- DTOs NewsAPI ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewsApiResponse(String status, Integer totalResults, List<NewsApiArticle> articles,
                                  String code, String message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewsApiArticle(NewsApiSource source, String author, String title, String description,
                                 String url, String urlToImage, String publishedAt, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewsApiSource(String id, String name) {}

    // --- DTO interne (inchangé pour compat avec NewsService) ---

    public record NewsRawItem(
            String guid,
            String title,
            String link,
            String imageUrl,
            String source,
            Long publishedTimestamp,
            String excerpt
    ) {}
}

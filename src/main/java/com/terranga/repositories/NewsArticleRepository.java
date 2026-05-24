package com.terranga.repositories;

import com.terranga.entities.NewsArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticleEntity, Long> {

    Optional<NewsArticleEntity> findByGuid(String guid);

    @Query("SELECT n FROM NewsArticleEntity n ORDER BY n.publishedTimestamp DESC")
    List<NewsArticleEntity> findAllOrderedByPublishedDesc();
}

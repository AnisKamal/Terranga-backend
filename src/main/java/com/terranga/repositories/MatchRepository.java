package com.terranga.repositories;

import com.terranga.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    @Query(value = """
        SELECT * FROM t_match
        WHERE timestamp >= :nowTimestamp
        ORDER BY timestamp ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<MatchEntity> findNextMatch(@Param("nowTimestamp") long nowTimestamp);

    /**
     * Tous les matchs à venir (timestamp >= maintenant), triés du plus proche au plus lointain.
     * Le premier item correspond au prochain match (identique à findNextMatch).
     */
    @Query(value = """
        SELECT * FROM t_match
        WHERE timestamp >= :nowTimestamp
        ORDER BY timestamp ASC
        """, nativeQuery = true)
    List<MatchEntity> findUpcomingMatches(@Param("nowTimestamp") long nowTimestamp);

    @Query(value = """
        SELECT * FROM t_match
        WHERE status_short = 'FT'
        ORDER BY timestamp DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MatchEntity> findLastFinishedMatches(@Param("limit") int limit);

    /**
     * Derniers matchs FT pour lesquels les buteurs n'ont pas encore été synchronisés.
     * Utilisé pour limiter les appels à /fixtures/events à un sous-ensemble.
     */
    @Query(value = """
        SELECT * FROM t_match
        WHERE status_short = 'FT'
          AND home_scorers IS NULL
          AND away_scorers IS NULL
        ORDER BY timestamp DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MatchEntity> findLastFinishedMatchesWithoutScorers(@Param("limit") int limit);

    Optional<MatchEntity> findByIdFixture(Long idFixture);

}
package com.terranga.repositories;

import com.terranga.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    @Modifying
    @Query(value = """
    INSERT INTO t_match (id_fixture, timestamp, date, referee, home_name, home_logo, away_name, away_logo, created_date, updated_date)
    VALUES (:idFixture, :timestamp, :date, :referee, :homeName, :homeLogo, :awayName, :awayLogo, NOW(), NOW())
    ON CONFLICT (id_fixture) DO UPDATE SET
        timestamp    = EXCLUDED.timestamp,
        date         = EXCLUDED.date,
        referee      = EXCLUDED.referee,
        home_name    = EXCLUDED.home_name,
        home_logo    = EXCLUDED.home_logo,
        away_name    = EXCLUDED.away_name,
        away_logo    = EXCLUDED.away_logo,
        updated_date = NOW()
    """, nativeQuery = true)
    void upsert(
            @Param("idFixture")  Long idFixture,
            @Param("timestamp")  Long timestamp,
            @Param("date")       String date,
            @Param("referee")    String referee,
            @Param("homeName")   String homeName,
            @Param("homeLogo")   String homeLogo,
            @Param("awayName")   String awayName,
            @Param("awayLogo")   String awayLogo
    );

}
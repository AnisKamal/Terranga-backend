package com.terranga.repositories;

import com.terranga.entities.FcmTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

    Optional<FcmTokenEntity> findByToken(String token);

    List<FcmTokenEntity> findAllByIsActiveTrue();

    @Modifying
    @Transactional
    @Query("UPDATE FcmTokenEntity f SET f.isActive = false WHERE f.token = :token")
    int deactivateToken(@Param("token") String token);
}

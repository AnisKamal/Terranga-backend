package com.terranga.repositories;

import com.terranga.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findByIdPlayer(Long idPlayer);
}

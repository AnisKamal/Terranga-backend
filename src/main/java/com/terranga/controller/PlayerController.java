package com.terranga.controller;

import com.terranga.dto.PlayerDetailsResponse;
import com.terranga.dto.PlayerResponse;
import com.terranga.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/players")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public List<PlayerResponse> getAllPlayers() {
        log.info("=================== call get all Players ==================");
        return playerService.getAllPlayers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDetailsResponse> getPlayerDetails(@PathVariable long id) {
        log.info("=================== call get PlayerDetails (id={}) ==================", id);
        return playerService.getPlayerDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

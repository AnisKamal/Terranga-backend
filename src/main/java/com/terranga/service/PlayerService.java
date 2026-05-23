package com.terranga.service;

import com.terranga.client.ApiFootballClient;
import com.terranga.common.DateUtilities;
import com.terranga.dto.PlayerDetailsApiResponse;
import com.terranga.dto.PlayerDetailsResponse;
import com.terranga.dto.PlayerResponse;
import com.terranga.dto.SquadApiResponse;
import com.terranga.entities.PlayerEntity;
import com.terranga.mapper.PlayerMapper;
import com.terranga.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private final ApiFootballClient apiFootballClient;
    private final PlayerRepository playerRepository;
    private final PlayerMapper mapper;

    @Value("${api.api-football.id.senegal-team}")
    private Integer idTeamSenegal;

    @Transactional
    public void syncSquad() {
        log.info("=== Début sync squad pour team={} ===", idTeamSenegal);
        SquadApiResponse squad = apiFootballClient.getSquad(idTeamSenegal);
        log.info("Réponse API /players/squads : {} entrée(s)",
                squad.response() == null ? "null" : squad.response().size());

        if (squad.response() == null || squad.response().isEmpty()) {
            log.warn("Squad vide ou null pour team={}. Sync abandonnée.", idTeamSenegal);
            return;
        }
        List<SquadApiResponse.Player> apiPlayers = squad.response().get(0).players();
        log.info("Nombre de joueurs reçus de l'API : {}", apiPlayers == null ? 0 : apiPlayers.size());

        if (apiPlayers == null) return;

        // Dédup par idPlayer : l'API peut renvoyer le même joueur plusieurs fois,
        // ce qui causerait un StaleStateException sur le second save() dans la même transaction.
        Map<Long, SquadApiResponse.Player> uniqueById = new LinkedHashMap<>();
        for (SquadApiResponse.Player p : apiPlayers) {
            if (p == null || p.id() == null) continue;
            uniqueById.putIfAbsent(p.id(), p);
        }
        log.info("Joueurs uniques après dédup : {}", uniqueById.size());

        for (SquadApiResponse.Player p : uniqueById.values()) {
            playerRepository.findByIdPlayer(p.id())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setName(p.name());
                                existing.setAge(p.age());
                                existing.setNumber(p.number());
                                existing.setPosition(p.position());
                                existing.setPhoto(p.photo());
                                playerRepository.save(existing);
                            },
                            () -> playerRepository.save(mapper.mapApiPlayerToEntity(p))
                    );
        }
        long total = playerRepository.count();
        log.info("=== Fin sync squad : {} joueurs en DB ===", total);
    }

    public List<PlayerResponse> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(mapper::mapEntityToDto)
                .toList();
    }

    public Optional<PlayerDetailsResponse> getPlayerDetails(long playerId) {
        int season = DateUtilities.currentSeason();
        PlayerDetailsApiResponse details = apiFootballClient.getPlayerDetails(playerId, season);
        if (details.response() == null || details.response().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.mapApiDetailsToDto(details.response().get(0)));
    }
}

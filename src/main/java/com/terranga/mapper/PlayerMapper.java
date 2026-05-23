package com.terranga.mapper;

import com.terranga.dto.PlayerDetailsApiResponse;
import com.terranga.dto.PlayerDetailsResponse;
import com.terranga.dto.PlayerResponse;
import com.terranga.dto.SquadApiResponse;
import com.terranga.entities.PlayerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    /**
     * Squad API → entité DB. Le champ `id` du JSON devient `idPlayer` en DB.
     * IMPORTANT : ignorer la PK héritée (BaseEntity.id) ET les champs d'audit,
     * sinon MapStruct mappe implicitement source.id → target.id (la PK) en plus
     * de target.idPlayer, ce qui casse la génération de séquence et provoque
     * un StaleStateException au save.
     */
    @Mapping(source = "id", target = "idPlayer")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    PlayerEntity mapApiPlayerToEntity(SquadApiResponse.Player apiPlayer);

    /** Entité DB → DTO liste mobile. Le champ `idPlayer` redevient `id` pour le mobile. */
    @Mapping(source = "idPlayer", target = "id")
    PlayerResponse mapEntityToDto(PlayerEntity entity);

    /**
     * API détails → DTO mobile à plat. La première entrée de statistics[] sert
     * de "club actuel" + stats principales (les autres entrées correspondent à
     * d'autres compétitions secondaires).
     */
    default PlayerDetailsResponse mapApiDetailsToDto(PlayerDetailsApiResponse.PlayerData data) {
        if (data == null || data.player() == null) return null;
        PlayerDetailsApiResponse.PlayerInfo p = data.player();
        PlayerDetailsApiResponse.Statistic stat = (data.statistics() != null && !data.statistics().isEmpty())
                ? data.statistics().get(0) : null;

        return new PlayerDetailsResponse(
                p.id(),
                p.name(),
                p.firstname(),
                p.lastname(),
                p.age(),
                p.birth() != null ? p.birth().date() : null,
                p.birth() != null ? p.birth().place() : null,
                p.nationality(),
                p.height(),
                p.weight(),
                p.injured(),
                p.photo(),
                stat != null && stat.team() != null ? stat.team().name() : null,
                stat != null && stat.team() != null ? stat.team().logo() : null,
                stat != null && stat.games() != null ? stat.games().appearances() : null,
                stat != null && stat.goals() != null ? stat.goals().total() : null,
                stat != null && stat.goals() != null ? stat.goals().assists() : null,
                stat != null && stat.games() != null ? stat.games().minutes() : null
        );
    }

    @Named("noop")
    default String noop(String s) { return s; }
}

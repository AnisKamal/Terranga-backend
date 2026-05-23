package com.terranga.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_player")
@Setter
@Getter
public class PlayerEntity extends BaseEntity {

    @Column(unique = true, nullable = false)
    private Long idPlayer;

    private String name;

    private Integer age;

    private Integer number;

    /** "Attacker", "Midfielder", "Defender", "Goalkeeper" */
    private String position;

    private String photo;
}

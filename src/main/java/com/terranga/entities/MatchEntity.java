package com.terranga.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_match")
@Setter
@Getter
public class MatchEntity extends BaseEntity{

    @Column(unique = true, nullable = false)
    private Long idFixture;

    private Long timestamp;

    private String date ;

    private String referee ;

    private String homeName;

    private String homeLogo;

    private String awayName;

    private String awayLogo;

}

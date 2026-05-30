package com.terranga.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Token FCM (Firebase Cloud Messaging) d'un appareil utilisateur.
 * Tokens anonymes (pas de lien user) — toutes les notifs sont broadcast à tous les actifs.
 */
@Entity
@Table(name = "t_fcm_token",
        indexes = { @Index(name = "idx_fcm_token", columnList = "token") })
@Getter
@Setter
@NoArgsConstructor
public class FcmTokenEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /** Info indicative sur l'appareil (ex: "Android 14", "iOS 17.2 iPhone 15"). Optionnel. */
    private String deviceInfo;

    /** false → token invalide (app désinstallée, expiré). Filtré lors de l'envoi. */
    private Boolean isActive = true;
}

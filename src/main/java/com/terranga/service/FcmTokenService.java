package com.terranga.service;

import com.terranga.dto.FcmTokenRequest;
import com.terranga.dto.FcmTokenResponse;
import com.terranga.entities.FcmTokenEntity;
import com.terranga.repositories.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    /** Enregistre un nouveau token ou réactive un token existant. */
    @Transactional
    public FcmTokenResponse registerToken(FcmTokenRequest request) {
        log.info("Enregistrement token FCM (deviceInfo={})", request.deviceInfo());

        FcmTokenEntity entity = fcmTokenRepository.findByToken(request.token())
                .orElseGet(() -> {
                    FcmTokenEntity e = new FcmTokenEntity();
                    e.setToken(request.token());
                    log.info("Nouveau token créé");
                    return e;
                });
        entity.setDeviceInfo(request.deviceInfo());
        entity.setIsActive(true);
        entity = fcmTokenRepository.save(entity);
        return toDto(entity);
    }

    /** Désactive un token (app désinstallée ou logout). */
    public void deactivateToken(String token) {
        int updated = fcmTokenRepository.deactivateToken(token);
        if (updated == 0) {
            log.info("Token à désactiver non trouvé");
        } else {
            log.info("Token désactivé");
        }
    }

    /** Tokens actifs pour broadcast. */
    public List<String> getAllActiveTokens() {
        return fcmTokenRepository.findAllByIsActiveTrue()
                .stream()
                .map(FcmTokenEntity::getToken)
                .toList();
    }

    private FcmTokenResponse toDto(FcmTokenEntity e) {
        return new FcmTokenResponse(
                e.getId(),
                e.getToken(),
                e.getDeviceInfo(),
                e.getIsActive(),
                e.getCreatedDate(),
                e.getUpdatedDate()
        );
    }
}

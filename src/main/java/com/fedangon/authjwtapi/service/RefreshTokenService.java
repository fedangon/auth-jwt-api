package com.fedangon.authjwtapi.service;

import com.fedangon.authjwtapi.config.RefreshTokenProperties;
import com.fedangon.authjwtapi.entity.RefreshTokenEntity;
import com.fedangon.authjwtapi.entity.UserEntity;
import com.fedangon.authjwtapi.exception.UnauthorizedException;
import com.fedangon.authjwtapi.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProperties refreshTokenProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, RefreshTokenProperties refreshTokenProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenProperties = refreshTokenProperties;
    }

    @Transactional
    public IssuedRefreshToken issue(UserEntity user, Instant now) {
        String rawToken = generateTokenValue();
        String tokenHash = hashToken(rawToken);

        Instant expiresAt = now.plus(refreshTokenProperties.getTtl());
        RefreshTokenEntity entity = new RefreshTokenEntity(user, tokenHash, expiresAt, now);
        refreshTokenRepository.save(entity);

        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    @Transactional
    public UserEntity validateActiveAndGetUser(String refreshToken, Instant now) {
        String tokenHash = hashToken(refreshToken);

        RefreshTokenEntity entity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("invalid_refresh_token", "Invalid refresh token."));

        if (!entity.isActiveAt(now)) {
            throw new UnauthorizedException("invalid_refresh_token", "Invalid refresh token.");
        }

        return entity.getUser();
    }

    @Transactional
    public void revokeIfActive(String refreshToken, Instant now) {
        String tokenHash = hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(entity -> {
            if (entity.isActiveAt(now)) {
                entity.revoke(now);
            }
        });
    }

    @Transactional
    public IssuedRefreshToken rotate(String refreshToken, Instant now) {
        String tokenHash = hashToken(refreshToken);

        RefreshTokenEntity entity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("invalid_refresh_token", "Invalid refresh token."));

        if (!entity.isActiveAt(now)) {
            throw new UnauthorizedException("invalid_refresh_token", "Invalid refresh token.");
        }

        entity.revoke(now);
        return issue(entity.getUser(), now);
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashToken(String token) {
        // Nao armazenar o refresh token em texto puro no banco
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }

        byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return toHex(hashed);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    public record IssuedRefreshToken(String token, Instant expiresAt) {
    }
}


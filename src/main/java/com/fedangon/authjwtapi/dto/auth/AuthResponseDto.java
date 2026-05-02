package com.fedangon.authjwtapi.dto.auth;

public record AuthResponseDto(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String refreshToken
) {
}


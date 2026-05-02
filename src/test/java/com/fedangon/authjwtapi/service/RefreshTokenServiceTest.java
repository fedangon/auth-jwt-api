package com.fedangon.authjwtapi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RefreshTokenServiceTest {

    @Test
    void hashToken_shouldBeDeterministicAndHex() {
        String token = "abc123";
        String hash1 = RefreshTokenService.hashToken(token);
        String hash2 = RefreshTokenService.hashToken(token);

        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
    }

    @Test
    void hashToken_shouldChangeWhenTokenChanges() {
        String hash1 = RefreshTokenService.hashToken("token-1");
        String hash2 = RefreshTokenService.hashToken("token-2");
        assertNotEquals(hash1, hash2);
    }
}


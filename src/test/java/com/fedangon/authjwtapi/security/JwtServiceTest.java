package com.fedangon.authjwtapi.security;

import com.fedangon.authjwtapi.config.JwtConfig;
import com.fedangon.authjwtapi.config.JwtProperties;
import com.fedangon.authjwtapi.entity.Role;
import com.fedangon.authjwtapi.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    @Test
    void generateAccessToken_shouldContainExpectedClaims() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("https://test-issuer");
        properties.setAccessTokenTtl(Duration.ofMinutes(5));
        properties.setClockSkew(Duration.ofSeconds(0));
        properties.setSecret(Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes()));

        JwtConfig config = new JwtConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        JwtEncoder encoder = config.jwtEncoder(secretKey);
        JwtDecoder decoder = config.jwtDecoder(secretKey, properties);

        JwtService jwtService = new JwtService(encoder, properties);

        UserEntity user = new UserEntity("user@example.com", "hash", "User", Instant.now(), Set.of(Role.USER));
        UUID userId = UUID.randomUUID();
        setField(user, "id", userId);

        Instant now = Instant.now();
        JwtService.AccessTokenResult result = jwtService.generateAccessToken(user, now);

        Jwt jwt = decoder.decode(result.token());
        assertEquals(properties.getIssuer(), jwt.getIssuer().toString());
        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals("user@example.com", jwt.getClaimAsString("email"));
        assertNotNull(jwt.getExpiresAt());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

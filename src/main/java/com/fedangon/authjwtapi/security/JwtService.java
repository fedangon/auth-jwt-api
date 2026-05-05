package com.fedangon.authjwtapi.security;

import com.fedangon.authjwtapi.config.JwtProperties;
import com.fedangon.authjwtapi.entity.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private static final Logger log = LogManager.getLogger(JwtService.class);

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public AccessTokenResult generateAccessToken(UserEntity user, Instant now) {
        Instant expiresAt = now.plus(jwtProperties.getAccessTokenTtl());

        List<String> roles = user.getRoles().stream().map(Enum::name).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("token_type", "access")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        long expiresInSeconds = Math.max(0, expiresAt.getEpochSecond() - now.getEpochSecond());
        log.debug("Access token gerado: userId={} expiresAt={} expiresInSeconds={}",
                user.getId(), expiresAt, expiresInSeconds);
        return new AccessTokenResult(tokenValue, expiresInSeconds);
    }

    public record AccessTokenResult(String token, long expiresInSeconds) {
    }
}

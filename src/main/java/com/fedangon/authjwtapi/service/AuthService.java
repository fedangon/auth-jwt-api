package com.fedangon.authjwtapi.service;

import com.fedangon.authjwtapi.dto.auth.AuthResponseDto;
import com.fedangon.authjwtapi.entity.UserEntity;
import com.fedangon.authjwtapi.exception.UnauthorizedException;
import com.fedangon.authjwtapi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserService userService,
            RefreshTokenService refreshTokenService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponseDto register(String email, String password, String fullName) {
        Instant now = Instant.now();

        String passwordHash = passwordEncoder.encode(password);
        UserEntity user = userService.createUser(email, passwordHash, fullName, now);

        return issueTokens(user, now);
    }

    @Transactional
    public AuthResponseDto login(String email, String password) {
        Instant now = Instant.now();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("invalid_credentials", "Invalid credentials.");
        }

        UserEntity user = userService.getByEmail(email);
        return issueTokens(user, now);
    }

    @Transactional
    public AuthResponseDto refresh(String refreshToken) {
        Instant now = Instant.now();

        UserEntity user = refreshTokenService.validateActiveAndGetUser(refreshToken, now);
        RefreshTokenService.IssuedRefreshToken rotatedRefresh = refreshTokenService.rotate(refreshToken, now);

        JwtService.AccessTokenResult accessToken = jwtService.generateAccessToken(user, now);
        return new AuthResponseDto("Bearer", accessToken.token(), accessToken.expiresInSeconds(), rotatedRefresh.token());
    }

    @Transactional
    public void logout(String refreshToken) {
        Instant now = Instant.now();
        refreshTokenService.revokeIfActive(refreshToken, now);
    }

    private AuthResponseDto issueTokens(UserEntity user, Instant now) {
        JwtService.AccessTokenResult accessToken = jwtService.generateAccessToken(user, now);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user, now);
        return new AuthResponseDto("Bearer", accessToken.token(), accessToken.expiresInSeconds(), refreshToken.token());
    }
}

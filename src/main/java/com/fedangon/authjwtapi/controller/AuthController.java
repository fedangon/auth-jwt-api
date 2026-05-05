package com.fedangon.authjwtapi.controller;

import com.fedangon.authjwtapi.dto.auth.AuthResponseDto;
import com.fedangon.authjwtapi.dto.auth.LoginRequestDto;
import com.fedangon.authjwtapi.dto.auth.RefreshTokenRequestDto;
import com.fedangon.authjwtapi.dto.auth.RegisterRequestDto;
import com.fedangon.authjwtapi.service.AuthService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("Requisicao de cadastro recebida: email={}", request.email());
        return authService.register(request.email(), request.password(), request.fullName());
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Requisicao de login recebida: email={}", request.email());
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("Requisicao de refresh recebida");
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("Requisicao de logout recebida");
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}

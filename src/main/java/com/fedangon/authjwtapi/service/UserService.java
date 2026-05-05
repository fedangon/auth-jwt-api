package com.fedangon.authjwtapi.service;

import com.fedangon.authjwtapi.entity.Role;
import com.fedangon.authjwtapi.entity.UserEntity;
import com.fedangon.authjwtapi.exception.ConflictException;
import com.fedangon.authjwtapi.exception.NotFoundException;
import com.fedangon.authjwtapi.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEntity createUser(String email, String passwordHash, String fullName, Instant now) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.warn("Cadastro rejeitado: email ja esta em uso: email={}", normalizedEmail);
            throw new ConflictException("email_already_in_use", "Email is already in use.");
        }

        UserEntity user = new UserEntity(
                normalizedEmail,
                passwordHash,
                fullName,
                now,
                Set.of(Role.USER)
        );

        UserEntity saved = userRepository.save(user);
        log.info("Usuario criado: userId={} email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public UserEntity getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user_not_found", "User not found."));
    }

    @Transactional(readOnly = true)
    public UserEntity getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("user_not_found", "User not found."));
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}

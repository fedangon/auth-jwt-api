package com.fedangon.authjwtapi.dto.user;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        String fullName,
        Set<String> roles,
        Instant createdAt
) {
}


package com.fedangon.authjwtapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @Email
        @NotBlank
        @Size(max = 320)
        String email,

        @NotBlank
        @Size(min = 1, max = 72)
        String password
) {
}


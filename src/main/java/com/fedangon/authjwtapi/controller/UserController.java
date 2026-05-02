package com.fedangon.authjwtapi.controller;

import com.fedangon.authjwtapi.dto.user.UserResponseDto;
import com.fedangon.authjwtapi.entity.UserEntity;
import com.fedangon.authjwtapi.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserEntity user = userService.getById(userId);

        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());

        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                roles,
                user.getCreatedAt()
        );
    }
}


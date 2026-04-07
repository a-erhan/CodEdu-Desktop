package com.codedu.dtos.user;

import com.codedu.models.user.Role;
import lombok.Builder;

@Builder
public record UserDTO(
    int id,
    String username,
    String email,
    Role role,
    boolean isActive,
    boolean emailVerified
) {}

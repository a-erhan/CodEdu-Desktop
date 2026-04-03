package com.codedu.dtos.user;

import lombok.Builder;

@Builder
public record UserRegisterDTO(
    String username,
    String email,
    String password
) {}

package com.codedu.dtos.user;

import lombok.Builder;

@Builder
public record UserLoginDTO(
    String email,
    String password
) {}

package com.codedu.dtos.user;

import com.codedu.models.user.ItemType;
import lombok.Builder;

@Builder
public record ItemDTO(
    int id,
    String name,
    String description,
    String iconURL,
    int price,
    ItemType type,
    boolean owned
) {}

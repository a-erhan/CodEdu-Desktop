package com.codedu.dtos.user;

import lombok.Builder;
import java.util.List;

@Builder
public record StoreDTO(
    int id,
    List<ItemDTO> availableItems
) {}

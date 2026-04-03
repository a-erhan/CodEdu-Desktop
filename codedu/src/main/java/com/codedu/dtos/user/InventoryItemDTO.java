package com.codedu.dtos.user;

import lombok.Builder;

@Builder
public record InventoryItemDTO(
    int id,
    String itemName,
    String itemDescription,
    String itemIconURL,
    int quantity,
    boolean isEquipped
) {}

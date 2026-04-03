package com.codedu.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDTO {
    private int id;
    private String itemName;
    private String itemDescription;
    private String itemIconURL;
    private int quantity;
    private boolean isEquipped;
}

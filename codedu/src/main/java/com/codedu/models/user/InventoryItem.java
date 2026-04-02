package com.codedu.models.user;

import com.codedu.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private int quantity;
    private boolean isEquipped;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_inventory_id", nullable = false)
    private UserInventory inventory;
}
package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInventory extends BaseEntity {

    @OneToOne(mappedBy = "inventory")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    @Builder.Default
    private List<InventoryItem> items = new ArrayList<>();

    public void addItem(InventoryItem item) {
        this.items.add(item);
    }
    public void removeItem(InventoryItem item) {
        this.items.remove(item);
    }
}
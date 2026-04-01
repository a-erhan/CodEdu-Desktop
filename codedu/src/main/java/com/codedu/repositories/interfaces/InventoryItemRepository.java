package com.codedu.repositories.interfaces;

import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.UserInventory;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends GenericRepository<InventoryItem> {

    List<InventoryItem> findByInventory(UserInventory inventory);

    Optional<InventoryItem> findByInventoryAndItem(UserInventory inventory, Item item);

    /**
     * Unequip all items of the given type for a specific inventory.
     * Used to enforce "only one equipped avatar at a time".
     */
    int unequipAllByInventoryAndType(int inventoryId, ItemType type);
}

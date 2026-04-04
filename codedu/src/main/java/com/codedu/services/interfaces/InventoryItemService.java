package com.codedu.services.interfaces;

import com.codedu.dtos.user.InventoryItemDTO;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.Item;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface InventoryItemService {

    List<InventoryItemDTO> getItemsForUser(User user);

    /** Entity version for controllers still using entity classes */
    List<InventoryItem> getItemEntitiesForUser(User user);

    Optional<InventoryItem> getById(int id);

    Optional<InventoryItem> findByUserAndItem(User user, Item item);

    void save(InventoryItem inventoryItem);

    void update(InventoryItem inventoryItem);

    void setEquipped(InventoryItem inventoryItem, boolean equipped);

    Optional<InventoryItem> getEquippedAvatar(User user);

    int getAiRequestBalance(User user);

    boolean consumeAiRequest(User user);
}

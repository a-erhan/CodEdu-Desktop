package com.codedu.services.interfaces;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    Optional<ItemDTO> getItemDTOById(int id);

    Optional<Item> getItemById(int id);
    void applyItemEffect(int userId, int itemId);

    List<ItemDTO> getAllItems();

    /** Entity version for controllers still using entity classes */
    List<Item> getAllItemEntities();

    List<ItemDTO> getItemsByType(ItemType type);

    Optional<ItemDTO> getItemByName(String name);

    void saveItem(Item item);

    void updateItem(Item item);
}

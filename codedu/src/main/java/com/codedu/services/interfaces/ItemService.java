package com.codedu.services.interfaces;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    Optional<ItemDTO> getItemDTOById(int id);

    Optional<Item> getItemById(int id);
    void applyItemEffect(int userId, int itemId);

    List<ItemDTO> getAllItems();
    boolean isEffectRedundant(User user, Item item);
    void applyItemEffect(User user, Item item);

    int getGrantedQuantity(Item item);

    List<Item> getAllItemEntities();

    List<ItemDTO> getItemsByType(ItemType type);

    Optional<ItemDTO> getItemByName(String name);

    void saveItem(Item item);

    void updateItem(Item item);
}

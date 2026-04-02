package com.codedu.services.interfaces;

import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    Optional<Item> getItemById(int id);

    List<Item> getAllItems();

    List<Item> getItemsByType(ItemType type);

    Optional<Item> getItemByName(String name);

    void saveItem(Item item);

    void updateItem(Item item);
}

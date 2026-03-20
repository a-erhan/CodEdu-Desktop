package com.codedu.repositories.interfaces;

import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends GenericRepository<Item> {
    List<Item> findByType(ItemType type);
    Optional<Item> findByName(String name);
}

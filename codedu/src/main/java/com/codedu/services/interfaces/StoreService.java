package com.codedu.services.interfaces;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.Store;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface StoreService {

    Optional<Store> getStoreWithItemsById(int id);

    Optional<Store> getDefaultStoreWithItems();

    List<ItemDTO> getCatalogItems();

    List<Item> getCatalogItemEntities();

    Optional<User> purchaseItem(int userId, int itemId);

    void saveStore(Store store);

    void updateStore(Store store);
}

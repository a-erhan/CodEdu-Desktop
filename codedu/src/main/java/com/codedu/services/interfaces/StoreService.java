package com.codedu.services.interfaces;

import com.codedu.models.user.Item;
import com.codedu.models.user.Store;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface StoreService {

    Optional<Store> getStoreWithItemsById(int id);

    /**
     * The primary storefront (lowest id, not deleted), with catalog items loaded.
     */
    Optional<Store> getDefaultStoreWithItems();

    /**
     * Items to show in the Store UI: default store catalog if configured and non-empty,
     * otherwise all non-deleted items from the item catalog.
     */
    List<Item> getCatalogItems();

    /**
     * Purchase one unit of the given item for the user (deduct tokens and add/increment inventory item).
     * Returns updated user with inventory + gameState loaded.
     */
    Optional<User> purchaseItem(int userId, int itemId);

    void saveStore(Store store);

    void updateStore(Store store);
}

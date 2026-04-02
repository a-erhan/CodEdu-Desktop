package com.codedu.repositories.interfaces;

import com.codedu.models.user.Store;

import java.util.Optional;

public interface StoreRepository extends GenericRepository<Store> {
    Optional<Store> findByIdWithItems(int id);

    Optional<Store> findFirstWithItemsOrderedById();
}

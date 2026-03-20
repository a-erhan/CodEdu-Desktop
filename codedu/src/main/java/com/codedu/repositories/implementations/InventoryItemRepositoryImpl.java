package com.codedu.repositories.implementations;

import com.codedu.models.user.InventoryItem;
import com.codedu.repositories.interfaces.InventoryItemRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class InventoryItemRepositoryImpl extends GenericRepositoryImpl<InventoryItem> implements InventoryItemRepository {

    public InventoryItemRepositoryImpl() {
        super(InventoryItem.class);
    }
}

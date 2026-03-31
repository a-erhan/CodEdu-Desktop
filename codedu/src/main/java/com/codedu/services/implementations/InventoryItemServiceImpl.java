package com.codedu.services.implementations;

import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.Item;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.InventoryItemRepository;
import com.codedu.services.interfaces.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getItemsForUser(User user) {
        if (user == null || user.getInventory() == null) {
            return List.of();
        }
        return inventoryItemRepository.findByInventory(user.getInventory());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getById(int id) {
        return inventoryItemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> findByUserAndItem(User user, Item item) {
        if (user == null || user.getInventory() == null || item == null) {
            return Optional.empty();
        }
        return inventoryItemRepository.findByInventoryAndItem(user.getInventory(), item);
    }

    @Override
    @Transactional
    public void save(InventoryItem inventoryItem) {
        inventoryItemRepository.save(inventoryItem);
    }

    @Override
    @Transactional
    public void update(InventoryItem inventoryItem) {
        inventoryItemRepository.update(inventoryItem);
    }

    @Override
    @Transactional
    public void setEquipped(InventoryItem inventoryItem, boolean equipped) {
        if (inventoryItem == null) {
            return;
        }
        inventoryItem.setEquipped(equipped);
        inventoryItemRepository.update(inventoryItem);
    }
}

package com.codedu.services.implementations;

import com.codedu.dtos.user.InventoryItemDTO;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
import com.codedu.models.user.UserInventory;
import com.codedu.repositories.interfaces.InventoryItemRepository;
import com.codedu.services.interfaces.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getItemsForUser(User user) {
        if (user == null || user.getInventory() == null) return List.of();

        return inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(ii -> !ii.isDeleted()) // Filter out deleted items
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getById(int id) {
        return inventoryItemRepository.findById(id).filter(ii -> !ii.isDeleted());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> findByUserAndItem(User user, Item item) {
        if (user == null || user.getInventory() == null || item == null) return Optional.empty();

        return inventoryItemRepository.findByInventoryAndItem(user.getInventory(), item)
                .filter(ii -> !ii.isDeleted());
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
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getEquippedAvatar(User user) {
        if (user == null || user.getInventory() == null) return Optional.empty();

        return inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(i -> !i.isDeleted() &&
                        i.getItem() != null &&
                        i.getItem().getType() == ItemType.AVATAR &&
                        i.isEquipped())
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public int getAiRequestBalance(User user) {
        if (user == null || user.getInventory() == null) return 0;

        return inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(i -> !i.isDeleted() && i.getItem() != null && i.getItem().getType() == ItemType.AI_USAGE)
                .mapToInt(InventoryItem::getQuantity)
                .sum();
    }

    @Override
    @Transactional
    public boolean consumeAiRequest(User user) {
        if (user == null || user.getInventory() == null) return false;

        // Find the first available pack that has uses left
        InventoryItem managed = inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(i -> !i.isDeleted() && i.getItem() != null &&
                        i.getItem().getType() == ItemType.AI_USAGE &&
                        i.getQuantity() > 0)
                .findFirst()
                .orElse(null);

        if (managed == null) return false;

        managed.setQuantity(managed.getQuantity() - 1);

        // Optional: If you want to delete the item when it reaches 0
        if (managed.getQuantity() <= 0) {
            managed.setDeleted(true);
        }

        inventoryItemRepository.update(managed);
        return true;
    }

    @Override
    @Transactional
    public void setEquipped(InventoryItem inventoryItem, boolean equipped) {
        if (inventoryItem == null || inventoryItem.getId() <= 0) return;

        InventoryItem managed = inventoryItemRepository.findById(inventoryItem.getId()).orElse(null);
        if (managed == null || managed.isDeleted() || managed.getItem() == null) return;

        // Only Avatars can be equipped
        if (managed.getItem().getType() != ItemType.AVATAR) {
            managed.setEquipped(false);
            inventoryItemRepository.update(managed);
            return;
        }

        // Handle un-equipping
        if (!equipped) {
            managed.setEquipped(false);
            inventoryItemRepository.update(managed);
            return;
        }

        // Logic for exclusive equipment:
        // 1. Unequip all other avatars in this inventory
        inventoryItemRepository.unequipAllByInventoryAndType(managed.getInventory().getId(), ItemType.AVATAR);

        // 2. Equip this one
        managed.setEquipped(true);
        inventoryItemRepository.update(managed);
    }

    private InventoryItemDTO toDTO(InventoryItem ii) {
        Item item = ii.getItem();
        return InventoryItemDTO.builder()
                .id(ii.getId())
                .itemName(item != null ? item.getName() : "Unknown")
                .itemDescription(item != null ? item.getDescription() : "")
                .itemIconURL(item != null ? item.getIconURL() : "")
                .quantity(ii.getQuantity())
                .isEquipped(ii.isEquipped())
                .build();
    }

    @Override
    @Transactional
    public void addOrIncrementItemQuantity(User user, Item item, int quantityDelta) {
        if (user == null || item == null || quantityDelta <= 0) {
            return;
        }
        UserInventory inv = user.getInventory();
        if (inv == null) {
            inv = new UserInventory();
            user.setInventory(inv);
        }
        int add = Math.max(1, quantityDelta);
        if (inv.getId() > 0) {
            Optional<InventoryItem> existing = findByUserAndItem(user, item);
            if (existing.isPresent()) {
                InventoryItem row = existing.get();
                row.setQuantity(row.getQuantity() + add);
                inventoryItemRepository.update(row);
                return;
            }
        }
        InventoryItem row = InventoryItem.builder().inventory(inv).item(item).quantity(add).build();
        inv.addItem(row);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getItemEntitiesForUser(User user) {
        if (user == null || user.getInventory() == null) return List.of();

        return inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(ii -> !ii.isDeleted())
                .collect(Collectors.toList());
    }
}
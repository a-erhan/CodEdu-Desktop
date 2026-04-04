package com.codedu.services.implementations;

import com.codedu.dtos.user.InventoryItemDTO;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
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
        if (user == null || user.getInventory() == null) {
            return List.of();
        }
        return inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getEquippedAvatar(User user) {
        if (user == null || user.getInventory() == null) return Optional.empty();
        return inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(i -> i.getItem() != null && i.getItem().getType() == ItemType.AVATAR && i.isEquipped())
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public int getAiRequestBalance(User user) {
        if (user == null || user.getInventory() == null) return 0;
        return inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(i -> i.getItem() != null && i.getItem().getType() == ItemType.AI_USAGE)
                .mapToInt(InventoryItem::getQuantity)
                .sum();
    }

    @Override
    @Transactional
    public boolean consumeAiRequest(User user) {
        if (user == null || user.getInventory() == null) return false;
        List<InventoryItem> aiItems = inventoryItemRepository.findByInventory(user.getInventory()).stream()
                .filter(i -> i.getItem() != null && i.getItem().getType() == ItemType.AI_USAGE && i.getQuantity() > 0)
                .collect(Collectors.toList());
        if (aiItems.isEmpty()) return false;
        InventoryItem target = aiItems.get(0);
        InventoryItem managed = inventoryItemRepository.findById(target.getId()).orElse(null);
        if (managed == null) return false;
        managed.setQuantity(managed.getQuantity() - 1);
        inventoryItemRepository.update(managed);
        return true;
    }

    @Override
    @Transactional
    public void setEquipped(InventoryItem inventoryItem, boolean equipped) {
        if (inventoryItem == null || inventoryItem.getId() <= 0) {
            return;
        }

        InventoryItem managed = inventoryItemRepository.findById(inventoryItem.getId()).orElse(null);
        if (managed == null || managed.isDeleted() || managed.getItem() == null) {
            return;
        }

        ItemType type = managed.getItem().getType();

        if (type != ItemType.AVATAR) {
            if (managed.isEquipped()) {
                managed.setEquipped(false);
                inventoryItemRepository.update(managed);
            }
            return;
        }

        if (!equipped) {
            managed.setEquipped(false);
            inventoryItemRepository.update(managed);
            return;
        }

        if (managed.getInventory() == null || managed.getInventory().getId() <= 0) {
            return;
        }

        inventoryItemRepository.unequipAllByInventoryAndType(managed.getInventory().getId(), ItemType.AVATAR);
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
    @Transactional(readOnly = true)
    public List<InventoryItem> getItemEntitiesForUser(User user) {
        if (user == null || user.getInventory() == null) {
            return List.of();
        }
        return inventoryItemRepository.findByInventory(user.getInventory());
    }
}
